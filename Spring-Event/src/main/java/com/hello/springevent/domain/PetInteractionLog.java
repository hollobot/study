package com.hello.springevent.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PetInteractionLog {

    /** 日志 ID。 */
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 交互动作类型。 */
    private String actionType;

    /** 交互道具编码，可为空。 */
    private String itemCode;

    /** 饱食度变化值。 */
    private int satietyDelta;

    /** 清洁度变化值。 */
    private int cleanDelta;

    /** 精力值变化值。 */
    private int energyDelta;

    /** 成长值变化值。 */
    private int growthDelta;

    /** 日志创建时间。 */
    private LocalDateTime createTime;
}
