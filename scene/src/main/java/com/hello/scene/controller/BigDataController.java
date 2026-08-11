package com.hello.scene.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hello.scene.entity.BigData;
import com.hello.scene.service.BigDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/excel")
@Tag(name = "百万 Excel 导出测试", description = "1 生成数据、2 全量查询导出、3 分页多线程导出")
@RequiredArgsConstructor
public class BigDataController {

    private static final int DEFAULT_PAGE_SIZE = 5_000;
    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final int SXSSF_WINDOW_SIZE = 100;

    private final BigDataService bigDataService;

    @GetMapping("/gen")
    @Operation(summary = "接口1：生产百万测试数据写入数据库", description = "count 传入 1000000 生成百万条数据")
    public String genData(@Parameter(description = "生成数据条数", example = "1000000")
                          @RequestParam(defaultValue = "1000000") Integer count) {
        Random random = new Random();
        List<BigData> batchList = new ArrayList<>(1000);
        for (int i = 0; i < count; i++) {
            BigData item = new BigData();
            item.setName("用户_" + i);
            item.setPhone("138" + String.format("%08d", random.nextInt(99999999)));
            item.setEmail("user" + i + "@test.com");
            item.setAddress("广东省深圳市龙华区 xxx 街道" + i + "号");
            batchList.add(item);
            if (batchList.size() >= 1000) {
                bigDataService.saveBatch(batchList);
                batchList.clear();
            }
        }
        if (!batchList.isEmpty()) {
            bigDataService.saveBatch(batchList);
        }
        return "成功生成数据:" + count;
    }

    @GetMapping("/badExport")
    @Operation(
            summary = "接口2：全量查询后快速导出",
            description = "不分页，先一次性查询全量数据，再使用 SXSSFWorkbook 流式写 Excel。数据 List 仍会占用堆内存，小堆可复现 OOM。"
    )
    public void badExport(HttpServletResponse response) throws IOException {
        List<BigData> allList = bigDataService.list(
                Wrappers.lambdaQuery(BigData.class).orderByAsc(BigData::getId)
        );

        setExcelResponse(response, "全量查询导出.xlsx");
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE)) {
            try {
                Sheet sheet = workbook.createSheet("数据");
                createHeader(sheet);
                writeRows(sheet, allList, 1);
                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        }
    }

    @GetMapping("/goodExport")
    @Operation(
            summary = "接口3：分页 + 多线程导出",
            description = "每批并发查询多个分页，主线程按页顺序写入 Excel，避免一次性加载百万数据。"
    )
    public void goodExport(HttpServletResponse response,
                           @Parameter(description = "每页条数", example = "5000")
                           @RequestParam(defaultValue = "5000") Integer pageSize,
                           @Parameter(description = "查询线程数", example = "4")
                           @RequestParam(defaultValue = "4") Integer threadCount) throws IOException {
        int size = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        int threads = threadCount == null || threadCount <= 0 ? DEFAULT_THREAD_COUNT : threadCount;

        long total = bigDataService.count();
        long totalPage = (total + size - 1) / size;

        setExcelResponse(response, "分页多线程导出.xlsx");
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE)) {
            try {
                Sheet sheet = workbook.createSheet("数据");
                createHeader(sheet);

                int rowNum = 1;
                long pageNo = 1;
                while (pageNo <= totalPage) {
                    List<Future<List<BigData>>> pageTasks = submitPageTasks(executor, pageNo, totalPage, size, threads);
                    pageNo += pageTasks.size();

                    for (Future<List<BigData>> pageTask : pageTasks) {
                        rowNum = writeRows(sheet, getPageData(pageTask), rowNum);
                    }
                }

                workbook.write(response.getOutputStream());
            } finally {
                workbook.dispose();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private List<BigData> queryPage(long pageNo, int pageSize) {
        Page<BigData> page = Page.of(pageNo, pageSize);
        page.setSearchCount(false);
        LambdaQueryWrapper<BigData> wrapper = Wrappers.lambdaQuery(BigData.class).orderByAsc(BigData::getId);
        return bigDataService.page(page, wrapper).getRecords();
    }

    private List<Future<List<BigData>>> submitPageTasks(ExecutorService executor,
                                                        long firstPage,
                                                        long totalPage,
                                                        int pageSize,
                                                        int threadCount) {
        List<Future<List<BigData>>> pageTasks = new ArrayList<>();
        for (int i = 0; i < threadCount && firstPage + i <= totalPage; i++) {
            long pageNo = firstPage + i;
            pageTasks.add(executor.submit(() -> queryPage(pageNo, pageSize)));
        }
        return pageTasks;
    }

    private List<BigData> getPageData(Future<List<BigData>> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("导出分页查询被中断", e);
        } catch (Exception e) {
            throw new IOException("导出分页查询失败", e);
        }
    }

    private int writeRows(Sheet sheet, List<BigData> dataList, int rowNum) {
        for (BigData data : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getId());
            row.createCell(1).setCellValue(data.getName());
            row.createCell(2).setCellValue(data.getPhone());
            row.createCell(3).setCellValue(data.getEmail());
            row.createCell(4).setCellValue(data.getAddress());
        }
        return rowNum;
    }

    private void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("姓名");
        headerRow.createCell(2).setCellValue("手机号");
        headerRow.createCell(3).setCellValue("邮箱");
        headerRow.createCell(4).setCellValue("地址");
    }

    private void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
    }
}
