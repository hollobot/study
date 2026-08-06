# Spring-Event

复现简历中“适趣 AI 中文（倒霉狗后端开发）”相关后端能力：

- Spring Event 解耦用户交互、奖励结算、交互日志
- Redisson 用户维度分布式锁，保护金币扣减和重复写入
- Redis Bitmap 存储年度签到状态
- 多线程扫描 + 幂等校验补发漏发奖励
- MyBatis XML 管理 SQL
- Knife4j / Swagger 测试接口

## 版本兼容说明

当前模块使用 Spring Boot 3.3.5：

```xml
<spring-boot.version>3.3.5</spring-boot.version>
```

原因是 Knife4j / springdoc OpenAPI 目前更适配 Spring Boot 3 / Spring Framework 6 生态。如果使用 Spring Boot 4.x，访问接口文档时可能出现：

```text
java.lang.NoSuchMethodError:
'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

这个异常不是业务代码问题，而是 OpenAPI 文档组件和 Spring Web 版本不兼容。处理方式：

- Spring Boot 使用 3.x 稳定版本。
- Web 依赖使用 `spring-boot-starter-web`。
- MyBatis starter 使用 `mybatis-spring-boot-starter:3.0.4`。
- 测试依赖使用 `spring-boot-starter-test`。

## 面试速记

这个项目可以这样介绍：

```text
我负责的是儿童阅读产品里的倒霉狗养成系统。用户会对倒霉狗做喂食、洗澡、睡觉、喝水、运动等交互动作。
这些动作本身只应该关心宠物状态变化，比如饱食度、清洁度、精力值、成长值。

但动作完成后，系统还要做很多后置逻辑，比如发金币、加经验、记录交互日志、更新任务进度、运营埋点。
如果这些逻辑都写在交互接口里，后续每加一个玩法都要改主流程，代码会越来越耦合，也容易影响核心链路。

所以我用 Spring Event 做事件驱动：主流程只更新状态并发布 PetInteractionEvent。
奖励发放、交互日志分别由不同监听器处理。后续如果要加成就、任务、亲密度，只需要新增监听器，不需要改 PetInteractionService。

并发方面，我用 Redisson 按用户维度加锁，防止同一个用户同时点击多个动作导致状态和资产重复变更。
奖励发放用 reward_record 表做幂等，避免客户端断线或补偿任务重复执行时重复发奖。
签到用 Redis Bitmap 存年度签到状态，用 bit 位表示某一天是否签到，空间占用很小，查询也很快。
```

## 为什么这样实现

### 1. 为什么用 Spring Event

不用事件时，交互主流程容易变成这样：

```text
更新宠物状态
-> 发金币
-> 加经验
-> 写日志
-> 更新任务
-> 更新成就
-> 运营埋点
-> 钉钉通知异常
```

问题：

- 主流程越来越长，任何后置逻辑异常都可能影响用户交互。
- 新增玩法时要频繁修改同一个 service，容易引入回归问题。
- 奖励、日志、任务、成就属于不同业务边界，写在一起耦合太高。

使用 Spring Event 后：

```text
PetInteractionService
-> 更新宠物状态
-> 发布 PetInteractionEvent

PetInteractionRewardListener 监听事件，负责奖励
PetInteractionLogListener 监听事件，负责日志
后续新增 TaskProgressListener，负责任务进度
后续新增 AchievementListener，负责成就
```

好处：

- 核心交互链路更短，只处理宠物状态。
- 后置能力可以独立新增、独立测试、独立回滚。
- 符合开闭原则：新增监听器扩展能力，不改主流程。
- 监听器可以按业务拆分，代码职责清晰。

### 2. 后续新增动作怎么做

如果只是新增一个动作，比如“摸头”：

1. 在 `PetInteractionAction` 枚举里加一项：

```java
TOUCH("摸头", 0, 0, 2, 3, 1, 1, 2, 1)
```

2. Swagger 调用时传：

```json
{
  "userId": 10001,
  "actionType": "TOUCH",
  "itemCode": null
}
```

不需要改 Controller，也不需要改 `PetInteractionService`。

如果新增的是后置能力，比如“完成每日互动任务”：

1. 新增一个监听器：

```java
@Component
@RequiredArgsConstructor
public class PetTaskProgressListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPetInteractionEvent(PetInteractionEvent event) {
        // 根据 event.userId() 和 event.action() 更新任务进度
    }
}
```

2. 不需要改原有交互接口。

这就是用 Spring Event 的核心价值：动作主流程和后置业务解耦。

### 3. 事务怎么处理

整个交互链路不是一个大事务，而是按职责拆成多个事务：

```text
T1：交互主事务
PetInteractionService.interact
-> UserLockTemplate.execute("pet-interaction", userId)
-> TransactionTemplate 开启事务
-> PetMapper.ensurePet
-> PetMapper.applyInteraction
-> publish PetInteractionEvent
-> T1 提交

T2：交互奖励监听器事务
PetInteractionRewardListener
-> @TransactionalEventListener(AFTER_COMMIT)
-> @Transactional(REQUIRES_NEW)
-> RewardRecordCommandService.createPending
-> RewardSettlementService.settle
-> T2 提交

T3：交互日志监听器事务
PetInteractionLogListener
-> @TransactionalEventListener(AFTER_COMMIT)
-> @Transactional(REQUIRES_NEW)
-> PetInteractionLogMapper.insertLog
-> T3 提交
```

控制点：

- `TransactionTemplate` 控制 T1，保证宠物状态更新和事件发布在同一个主事务内。
- `@TransactionalEventListener(AFTER_COMMIT)` 控制监听器执行时机，只有 T1 提交成功后才执行。
- `@Transactional(REQUIRES_NEW)` 控制监听器独立事务，奖励和日志不会混进 T1。
- `RewardSettlementService.settle` 内部还有 `@Transactional`，发奖时会锁定奖励记录并更新资产。
- Redisson 锁包在 T1 外层，保证同一个用户的交互主链路串行执行。

代码对应关系：

| 事务 | 代码位置 | 做什么 | 失败影响 |
| --- | --- | --- | --- |
| T1 主事务 | `PetInteractionService.interact` + `TransactionTemplate` | 更新宠物状态、发布事件 | T1 回滚后，监听器不会执行 |
| T2 奖励事务 | `PetInteractionRewardListener` | 创建奖励记录、结算奖励 | 不回滚宠物状态，可由补偿任务处理 |
| T3 日志事务 | `PetInteractionLogListener` | 写交互日志 | 不回滚宠物状态和奖励 |
| 补偿事务 | `RewardRepairService` -> `RewardSettlementService.settle` | 扫描 PENDING 奖励并重新结算 | 幂等处理，避免重复发奖 |

为什么不把所有东西放一个事务：

- 交互状态是核心链路，要短。
- 奖励、日志、任务、埋点属于后置能力，不能让它们拖慢或污染主流程。
- 日志失败不应该导致用户交互失败。
- 奖励失败可以通过 `sq_reward_record` 的 `PENDING` 状态补偿。

失败场景：

```text
场景 1：T1 更新宠物状态失败
结果：T1 回滚，PetInteractionEvent 不会在 AFTER_COMMIT 阶段触发，奖励和日志都不执行。

场景 2：T1 成功，T2 奖励监听器失败
结果：宠物状态已经成功，不回滚；奖励记录如果停留 PENDING，后续 /api/reward/repair 补偿。

场景 3：T1 成功，T3 日志监听器失败
结果：不影响宠物状态和奖励；日志属于可补、可降级的非核心数据。

场景 4：补偿任务重复执行
结果：RewardSettlementService 会 SELECT ... FOR UPDATE，并检查状态是否 SUCCESS，避免重复发奖。
```

为什么事件发布在 T1 里：

```text
eventPublisher.publishEvent(...) 放在 TransactionTemplate 内。
这样 @TransactionalEventListener 能绑定当前事务。
如果 T1 commit，监听器在 AFTER_COMMIT 执行。
如果 T1 rollback，监听器不会执行。
```

为什么监听器用 `REQUIRES_NEW`：

```text
AFTER_COMMIT 触发时，主事务已经结束。
监听器要写奖励表、资产表、日志表，所以需要自己开启新事务。
REQUIRES_NEW 能让每个监听器独立提交，互不影响。
```

为什么奖励结算还能补偿：

```text
奖励通过 sq_reward_record 记录状态：
PENDING  -> 待发放
SUCCESS  -> 已发放

补偿任务只扫描 PENDING。
结算时 SELECT ... FOR UPDATE 锁住奖励记录。
如果已经是 SUCCESS，直接跳过。
```

面试可以这样答：

```text
整个链路不是一个大事务。
用户交互状态变更是主事务，由 TransactionTemplate 控制；事件发布也在这个事务内。
监听器用 @TransactionalEventListener(AFTER_COMMIT)，保证只有主事务提交成功后才执行。
奖励监听器和日志监听器用 REQUIRES_NEW 开独立事务，避免后置逻辑影响主流程。
如果奖励监听器失败，reward_record 会保留 PENDING 状态，后续补偿任务重新结算。
```

注意：

```text
如果某个后置逻辑必须和主流程强一致，比如扣金币购买商品，就不要放到事件监听器里。
购买里的扣金币和建订单仍然放在 PurchaseService 主事务中。
```

### 4. 监听器怎么判断是哪个动作

请求传入：

```json
{
  "userId": 10001,
  "actionType": "BATH",
  "itemCode": "SOAP"
}
```

`PetInteractionService` 会把字符串转成枚举：

```java
PetInteractionAction action = PetInteractionAction.valueOf(actionType)
```

然后事件直接携带动作枚举：

```java
new PetInteractionEvent(userId, action, itemCode, bizId)
```

监听器判断动作时不解析请求参数，而是使用：

```java
event.action()
event.action().name()
event.action().getCoinReward()
event.action().getSatietyDelta()
```

当前设计里，动作相关配置集中在 `PetInteractionAction`：

```text
FEED      饱食度 + 奖励
BATH      清洁度 + 奖励
SLEEP     精力值 + 奖励
DRINK     饱食度/精力值 + 奖励
EXERCISE  消耗饱食度/精力值，增加成长值
PLAY      均衡增加互动收益
```

这样做的好处：

- 动作配置集中，避免散落在多个 if/else 里。
- 监听器拿到的是结构化动作，不依赖 Controller 入参字符串。
- 新增动作时优先改枚举，不改监听器主逻辑。

如果某个监听器只关心特定动作，可以这样写：

```java
if (event.action() != PetInteractionAction.BATH) {
    return;
}
// 只处理洗澡后的逻辑
```

### 5. 后续添加维护事件怎么处理

这里分两种情况。

第一种：还是由“用户交互动作”触发的维护逻辑，比如：

- 更新每日任务
- 更新成就
- 增加亲密度
- 运营埋点
- 检查宠物状态是否需要提醒

这种不需要新增事件，直接新增监听器监听 `PetInteractionEvent`：

```java
@Component
@RequiredArgsConstructor
public class PetCareTaskListener {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPetInteractionEvent(PetInteractionEvent event) {
        if (event.action() == PetInteractionAction.BATH) {
            // 维护类逻辑：例如完成“给倒霉狗洗澡一次”的任务
        }
    }
}
```

这种扩展不需要改：

```text
PetController
PetInteractionService
PetInteractionRewardListener
PetInteractionLogListener
```

第二种：不是用户交互触发，而是系统定时维护触发，比如：

- 每天凌晨降低饱食度、清洁度、精力值
- 长时间未互动后触发提醒
- 定时扫描异常状态并修复

这种建议单独建维护事件，例如：

```java
public record PetDailyMaintainEvent(Long userId, String maintainType) {
}
```

然后由定时任务或维护接口发布：

```java
eventPublisher.publishEvent(new PetDailyMaintainEvent(userId, "DAILY_DECAY"));
```

再新增对应监听器：

```java
@Component
public class PetDailyMaintainListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMaintainEvent(PetDailyMaintainEvent event) {
        // 处理每日衰减、提醒、修复等维护逻辑
    }
}
```

面试可以这样答：

```text
如果维护逻辑是用户交互后的扩展，比如任务、成就、亲密度，我直接新增 PetInteractionEvent 的监听器。
如果维护逻辑是系统主动触发，比如每日状态衰减、异常修复，我会单独定义 PetDailyMaintainEvent，避免把不同来源的事件混在一起。
判断标准是事件语义：同一件事实用同一个事件，不同来源、不同生命周期就拆成新事件。
```

### 6. event 包文件说明

`event` 包只放事件对象和事件监听器，核心原则是：

```text
Event 表示“已经发生的业务事实”
Listener 表示“这个事实发生后，我要额外做什么”
```

当前文件作用：

| 文件 | 类型 | 作用 | 什么时候用 |
| --- | --- | --- | --- |
| `PetInteractionEvent` | 事件 | 表示用户完成了一次倒霉狗交互 | 喂食、洗澡、睡觉、喝水、运动、玩耍后发布 |
| `PetInteractionRewardListener` | 监听器 | 监听交互事件，根据动作配置创建并发放奖励 | 交互完成后给金币、积分、经验、成长值 |
| `PetInteractionLogListener` | 监听器 | 监听交互事件，写入交互日志 | 后续做行为统计、运营分析 |
| `RewardSettlementEvent` | 事件 | 表示某条奖励记录需要结算 | 签到、购买等已经创建 reward_record 的场景 |
| `RewardSettlementListener` | 监听器 | 监听奖励结算事件，统一调用奖励结算服务 | 复用通用发奖逻辑 |

调用关系：

```text
用户交互接口
-> PetInteractionService 更新宠物状态
-> 发布 PetInteractionEvent
-> PetInteractionRewardListener 发奖励
-> PetInteractionLogListener 写日志

签到/购买等场景
-> 创建 RewardRecord
-> 发布 RewardSettlementEvent
-> RewardSettlementListener 统一结算奖励
```

#### 新增一个交互后的后置能力

比如新增“交互后更新每日任务进度”：

1. 新增监听器类。

```java
@Component
@RequiredArgsConstructor
public class PetTaskProgressListener {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPetInteractionEvent(PetInteractionEvent event) {
        // 通过 event.userId() 找到用户
        // 通过 event.action() 判断 FEED/BATH/SLEEP 等动作
        // 更新每日任务进度
    }
}
```

2. 不需要修改：

```text
PetController
PetInteractionService
PetInteractionRewardListener
PetInteractionLogListener
```

适合放到 `PetInteractionEvent` 监听器里的逻辑：

- 每日任务进度
- 成就进度
- 亲密度增长
- 运营埋点
- 用户行为统计
- 交互后提醒检查

#### 新增一个动作

比如新增“摸头”动作：

1. 在 `PetInteractionAction` 加枚举：

```java
TOUCH("摸头", 0, 0, 2, 3, 1, 1, 2, 1)
```

2. 前端或 Swagger 传：

```json
{
  "userId": 10001,
  "actionType": "TOUCH",
  "itemCode": null
}
```

监听器不需要改，因为它们通过 `event.action()` 获取动作配置。

#### 新增一个独立事件

如果不是用户交互后的后置逻辑，而是另一个业务事实，就新增事件。

例如系统每日维护：

```java
public record PetDailyMaintainEvent(Long userId, String maintainType) {
}
```

对应监听器：

```java
@Component
public class PetDailyMaintainListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMaintainEvent(PetDailyMaintainEvent event) {
        // 每日衰减、状态修复、长时间未互动提醒
    }
}
```

判断是否新增事件的标准：

```text
如果还是“用户完成交互后”的扩展，用 PetInteractionEvent。
如果是“系统定时维护、外部消息、后台运营操作”等不同业务事实，新增独立 Event。
```

面试可以这样答：

```text
event 包里我把事件对象和监听器分开。
PetInteractionEvent 表示用户完成一次交互，奖励、日志、任务、成就都可以监听它扩展。
RewardSettlementEvent 表示某条奖励需要结算，签到和购买这种已经创建奖励记录的场景可以复用。
后续加动作改枚举，后续加交互后的能力加监听器，后续加新的业务事实才新增 Event。
```

### 7. 为什么用 Redisson 用户锁

用户可能连续点击交互或购买按钮，例如同时触发两次购买、两次签到、多个交互动作。

如果没有锁，可能出现：

- 金币余额同时判断足够，导致重复扣减或超买。
- 宠物状态并发更新，后提交的数据覆盖先提交的数据。
- 同一个奖励被重复创建或重复发放。

这里按用户维度加锁：

```text
shiqu:user:{userId}:pet-interaction
shiqu:user:{userId}:purchase
shiqu:user:{userId}:sign-in
```

好处：

- 只串行化同一个用户的关键操作，不影响其他用户。
- 粒度比全局锁小，性能更好。
- 配合数据库幂等约束，形成“双保险”。

### 8. 为什么奖励表要做幂等

事件监听、补偿任务、客户端重试都可能导致同一业务重复执行。

所以 `sq_reward_record` 使用：

```text
UNIQUE KEY uk_reward_biz (biz_type, biz_id)
```

含义：

- 同一个业务动作只允许有一条奖励记录。
- 重复请求只会复用原奖励记录。
- 补偿任务扫描 `PENDING` 奖励时，即使重复执行，也不会重复发奖。

### 9. 奖励统一补发怎么设计

奖励发放不是直接改用户资产，而是先落一条奖励记录：

```text
sq_reward_record
biz_type        奖励来源，比如 PET_INTERACTION / SIGN_IN / PURCHASE
biz_id          业务幂等 ID，比如 userId:action:uuid 或 order:1
coin_delta      金币变化
point_delta     积分变化
experience_delta 经验变化
growth_delta    成长值变化
status          PENDING / SUCCESS
```

状态流转：

```text
创建奖励记录
-> status = PENDING
-> RewardSettlementService.settle 发放奖励
-> 更新用户资产 / 宠物成长值
-> status = SUCCESS
```

统一补发的核心思想：

```text
正常发奖和补偿发奖都走 RewardSettlementService.settle。
补偿任务不自己写资产，只负责找到 PENDING 记录并重新调用 settle。
```

完整链路：

```text
业务动作完成
-> RewardRecordCommandService.createPending
-> 插入 sq_reward_record(status = PENDING)
-> RewardSettlementService.settle
-> SELECT ... FOR UPDATE 锁住奖励记录
-> 如果 status = SUCCESS，直接跳过
-> 如果 status = PENDING，发放金币/积分/经验/成长值
-> RewardRecordMapper.markSuccess
```

补偿接口链路：

```text
POST /api/reward/repair
-> RewardController.repair
-> RewardRepairService.repair
-> RewardRecordMapper.selectPending(limit)
-> ExecutorService 多线程处理
-> RewardSettlementService.settle(rewardRecordId)
-> SELECT ... FOR UPDATE
-> 成功发放后标记 SUCCESS
```

为什么能避免重复发奖：

1. 创建阶段有唯一键：

```text
UNIQUE KEY uk_reward_biz (biz_type, biz_id)
```

同一个业务动作不能创建多条奖励记录。

2. 结算阶段有行锁：

```sql
SELECT ...
FROM sq_reward_record
WHERE id = ?
FOR UPDATE
```

同一条奖励记录同一时间只能有一个线程结算。

3. 结算前检查状态：

```text
如果 status = SUCCESS，说明已经发过，直接 return false。
如果 status = PENDING，才真正发放。
```

哪些情况会靠补偿兜底：

- 客户端断线，用户没有拿到奖励结果。
- 事件监听器执行到一半服务重启。
- 奖励记录已创建，但资产更新失败。
- 多个服务实例中某个实例处理中断。
- 后续切 MQ 后，消息消费失败或超时。

为什么要统一补发，而不是每个业务自己补：

- 签到、购买、交互奖励都可以抽象成一条 `RewardRecord`。
- 所有奖励都复用 `RewardSettlementService.settle`。
- 幂等、加锁、状态判断只写一套，减少重复 bug。
- 面试时能体现“最终一致性 + 幂等补偿”的思路。

面试可以这样答：

```text
我没有直接在业务里改用户资产，而是先创建 reward_record，状态是 PENDING。
正常发奖和补偿发奖都走同一个 RewardSettlementService.settle。
settle 会 SELECT FOR UPDATE 锁住奖励记录，先判断状态，如果已经 SUCCESS 就跳过。
如果还是 PENDING，才更新资产并把奖励状态改成 SUCCESS。

这样即使监听器失败、服务重启、客户端重试，补偿任务只要扫描 PENDING 记录重新结算即可。
唯一键 uk_reward_biz 保证同一业务动作只会有一条奖励记录，FOR UPDATE 和 SUCCESS 状态判断保证不会重复发奖。
```

### 10. 为什么签到用 Redis Bitmap

签到是典型的“某用户某天是否做过”的布尔状态。

如果用 MySQL 每天一行：

```text
user_id | sign_date
```

一年 365 天就最多 365 行。

Bitmap 方案：

```text
key: shiqu:sign-in:2026:10001
第 1 天 -> bit 0
第 2 天 -> bit 1
第 190 天 -> bit 189
```

好处：

- 一个用户一年只需要 365/366 个 bit。
- 判断是否签到是位运算，速度快。
- 查询全年签到日历时，只需要遍历年度 bit。

## 面试常见追问

### Q1：Spring Event 是同步还是异步？

当前实现默认是同步监听，但使用了 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`，也就是主事务提交后再执行监听器逻辑。

面试可以这样答：

```text
这里我主要用 Spring Event 做业务解耦，而不是单纯追求异步。
当前监听器在事务提交后执行，保证只有宠物状态变更成功后才会发奖励和写日志。
如果后续日志、埋点这类逻辑量变大，可以给事件监听器配合 @Async 或消息队列进一步异步化。
```

### Q2：如果监听器失败怎么办？

当前有奖励补偿接口：

```http
POST /api/reward/repair
```

面试可以这样答：

```text
奖励创建和发放通过 reward_record 表记录状态。
如果客户端断线、监听器异常或服务中断导致奖励停留在 PENDING，补偿任务会扫描 PENDING 记录重新结算。
结算前会 SELECT FOR UPDATE，并且成功后把状态改成 SUCCESS，避免重复发放。
```

### Q3：为什么不用直接方法调用？

直接调用适合简单流程，但这个场景后置逻辑会持续变多。

面试可以这样答：

```text
如果只是发一个奖励，直接调用方法也可以。
但倒霉狗交互后面会挂很多能力：奖励、日志、任务、成就、运营埋点。
这些逻辑变化频率不同，属于不同业务边界。
用事件后，PetInteractionService 不需要知道有哪些后置业务，只发布事件即可。
```

### Q4：什么时候不用 Spring Event？

面试可以这样答：

```text
如果逻辑强依赖返回结果，或者必须在同一个事务里强一致完成，我不会用 Spring Event 拆开。
比如扣金币和创建订单必须在购买主流程里完成。
Spring Event 更适合“动作已经发生后”的扩展逻辑，比如奖励、日志、任务进度、通知。
```

### Q5：后续从 Spring Event 换 MQ 难不难？

面试可以这样答：

```text
不难。现在已经把业务抽象成 PetInteractionEvent。
如果流量变大，需要跨服务异步处理，可以把发布 Spring Event 的地方替换成发送 MQ 消息。
监听器里的奖励、日志逻辑也可以迁移成 MQ Consumer，业务边界基本不用重新拆。
```

## 准备工作

1. 创建数据库并初始化表结构：

```sql
source src/main/resources/sql/shiqu_ai.sql;
```

如果本地已经执行过旧版 SQL，建议先删除 `shiqu_ai_demo` 后重新执行脚本，或手动给 `sq_pet_profile` 补充 `clean_value`、`energy_value` 两列并创建 `sq_pet_interaction_log` 表。

2. 按本地环境调整 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shiqu_ai_demo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

redis:
  host: 127.0.0.1
  port: 6379
  password: 123456
```

3. 启动项目后访问 Knife4j / Swagger：

Knife4j 页面：

```text
http://localhost:8080/doc.html
```

Swagger UI：

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON：

```text
http://localhost:8080/v3/api-docs
```

## 接口列表

### 1. 和倒霉狗交互

```http
POST /api/pet/interact
```

作用：

- 支持喂食、洗澡、睡觉、喝水、运动、玩耍等动作
- 主流程只更新倒霉狗状态并发布 `PetInteractionEvent`
- 奖励发放由 `PetInteractionRewardListener` 处理
- 交互日志由 `PetInteractionLogListener` 处理
- 后续新增亲密度、任务进度、运营埋点等逻辑时，只需要新增监听器，不需要改交互主流程

请求示例：

```json
{
  "userId": 10001,
  "actionType": "BATH",
  "itemCode": "SOAP"
}
```

支持动作：

```text
FEED      喂食
BATH      洗澡
SLEEP     睡觉
DRINK     喝水
EXERCISE  运动
PLAY      玩耍
```

调用链路：

```text
PetController.interact
-> PetInteractionService.interact
-> UserLockTemplate.execute("pet-interaction", userId)
-> TransactionTemplate                         [T1 主事务开始]
-> PetMapper.ensurePet                         [T1]
-> PetMapper.applyInteraction                  [T1]
-> publish PetInteractionEvent                 [T1]
-> 主事务提交                                  [T1 主事务结束]
-> PetInteractionRewardListener AFTER_COMMIT   [T2 新事务开始]
-> RewardRecordCommandService.createPending    [T2]
-> RewardSettlementService.settle              [T2]
-> PetInteractionRewardListener 提交           [T2 新事务结束]
-> PetInteractionLogListener AFTER_COMMIT      [T3 新事务开始]
-> PetInteractionLogMapper.insertLog           [T3]
-> PetInteractionLogListener 提交              [T3 新事务结束]
```

兼容喂食入口：

```http
POST /api/pet/feed
```

请求示例：

```json
{
  "userId": 10001,
  "foodCode": "MEAT"
}
```

该接口内部会转成：

```json
{
  "userId": 10001,
  "actionType": "FEED",
  "itemCode": "MEAT"
}
```

### 2. 每日签到

```http
POST /api/sign-in
```

作用：

- 使用 Redis Bitmap 记录用户某年某天是否签到
- 创建签到奖励记录
- 发布奖励结算事件

Redis key：

```text
shiqu:sign-in:{year}:{userId}
```

请求示例：

```json
{
  "userId": 10001
}
```

调用链路：

```text
SignInController.signIn
-> SignInService.signIn
-> UserLockTemplate.execute("sign-in", userId)
-> RBitSet.get(dayOfYear - 1)
-> RewardRecordCommandService.createPending
-> RBitSet.set(dayOfYear - 1, true)
-> publish RewardSettlementEvent
-> RewardSettlementService.settle
```

### 3. 查询签到日历

```http
GET /api/sign-in/calendar/{userId}?year=2026
```

作用：

- 读取 Redis Bitmap
- 返回指定年份已签到日期列表和签到天数

调用链路：

```text
SignInController.calendar
-> SignInService.getCalendar
-> RBitSet.get(offset)
-> SignInCalendarResponse
```

### 4. 购买虚拟商品

```http
POST /api/shop/purchase
```

作用：

- 查询虚拟商品
- 使用用户维度 Redisson 锁保护并发购买
- 扣减金币并生成订单
- 创建购买奖励记录，通过事件发放购买积分

请求示例：

```json
{
  "userId": 10001,
  "productId": 1
}
```

调用链路：

```text
ShopController.purchase
-> PurchaseService.purchase
-> UserLockTemplate.execute("purchase", userId)
-> ProductMapper.selectById
-> UserAssetMapper.ensureUserAsset
-> UserAssetMapper.deductCoin
-> PurchaseOrderMapper.insertOrder
-> RewardRecordCommandService.createPending
-> publish RewardSettlementEvent
-> RewardSettlementService.settle
```

### 5. 补发漏发奖励

```http
POST /api/reward/repair
```

作用：

- 扫描 `sq_reward_record` 中 `PENDING` 状态的奖励
- 使用固定线程池并发补偿
- 补偿时复用 `RewardSettlementService.settle`
- 每条奖励结算前 `SELECT ... FOR UPDATE`
- 已经成功的奖励直接跳过，避免重复发放
- 适用于交互、签到、购买等所有写入 `sq_reward_record` 的奖励

请求示例：

```json
{
  "limit": 100,
  "threadCount": 4
}
```

调用链路：

```text
RewardController.repair
-> RewardRepairService.repair
-> RewardRecordMapper.selectPending
-> ExecutorService.invokeAll
-> RewardSettlementService.settle
-> RewardRecordMapper.selectByIdForUpdate
-> status = SUCCESS 时跳过
-> status = PENDING 时发放奖励
-> UserAssetMapper.addReward / PetMapper.addGrowth
-> RewardRecordMapper.markSuccess
```

### 6. 查询用户资产和宠物状态

```http
GET /api/user/{userId}/assets
```

作用：

- 初始化用户资产和倒霉狗档案
- 查询金币、积分、经验、等级、饱食度、清洁度、精力值、成长值

调用链路：

```text
UserAssetController.assets
-> UserOverviewService.getOverview
-> UserAssetMapper.ensureUserAsset
-> PetMapper.ensurePet
-> PetMapper.selectOverview
```

## 核心表

- `sq_user_asset`：用户金币、积分、经验
- `sq_pet_profile`：倒霉狗等级、饱食度、清洁度、精力值、成长值
- `sq_pet_interaction_log`：倒霉狗交互日志
- `sq_virtual_product`：虚拟商品
- `sq_purchase_order`：购买订单
- `sq_reward_record`：奖励记录和幂等状态

奖励幂等由 `sq_reward_record.uk_reward_biz (biz_type, biz_id)` 保证。

## MyBatis 文件

Mapper 接口：

```text
src/main/java/com/hello/springevent/mapper
```

XML SQL：

```text
src/main/resources/mapper
```
