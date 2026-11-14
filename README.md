# 2026 日历（Android）

> 中文为默认语言，英文版见下方 [English Version](#english-version)。

一个纯原生（Java + 自绘 Canvas）、零第三方依赖、无需联网的安卓日历 App：一眼看全 2026 年所有法定节假日，支持**月视图 / 年视图**切换。

## 功能

- **月视图**：7×6 日历网格，法定节假日红色圆角块 + 假期名称标签，调休上班日蓝色标记，今天描环高亮；左右滑动切换月份，点击日期看当天状态。
- **年视图**：12 个迷你月历（3 列 × 4 行），全年放假 / 调休安排一屏看全；点任意月份跳到月视图；下方按时间顺序列出 7 个假期的完整区间与天数。
- **底部信息卡**：本月放假天数、调休天数、假期区间、距下一个假期的倒计时。
- **顶部「今天」按钮**：一键回到当前月份并选中今天。
- 全年概览数据：放假 33 天、7 个节假日、调休上班 6 天。

数据严格取自《国务院办公厅关于2026年部分节假日安排的通知》（国办发明电〔2025〕7号）。

## 2026 年放假数据

| 节假日 | 放假区间 | 天数 | 调休上班 |
|---|---|---|---|
| 元旦 | 1月1日–1月3日 | 3 | 1月4日 |
| 春节 | 2月15日–2月23日 | 9 | 2月14日、2月28日 |
| 清明节 | 4月4日–4月6日 | 3 | — |
| 劳动节 | 5月1日–5月5日 | 5 | 5月9日 |
| 端午节 | 6月19日–6月21日 | 3 | — |
| 中秋节 | 9月25日–9月27日 | 3 | — |
| 国庆节 | 10月1日–10月7日 | 7 | 9月20日、10月10日 |

## 技术要点

- 语言 / 框架：Java + Android 原生 View，**无任何第三方依赖、无 Jetpack Compose、无网络权限**
- 界面：全部由 `Activity` 代码构建，日历格子用 `Canvas` 自绘（`MonthView` / `YearView`），安装包仅约 60 KB
- 兼容性：`minSdk 21`（Android 5.0+），`targetSdk 34`
- 数据层：`HolidayData` 在内存中构建全年 366 天模型，涵盖工作日 / 节假日 / 调休上班三种类型

## 构建方式

### 方式一：Android Studio

```bash
# 打开工程目录即可 Run / Build APK
android-2026-calendar/
```

AGP 8.1.4 + Gradle 8，`compileSdk 34`。

### 方式二：命令行无 Gradle 构建

`build.sh` 使用 SDK 自带的 `aapt2 + javac + d8 + apksigner` 手工打包，无需 Gradle 与依赖下载：

```bash
export ANDROID_SDK=/path/to/android-sdk   # 需含 platforms/android-34/android.jar 与 build-tools/34.0.0
./build.sh
```

产物：`app/build/outputs/apk/2026日历.apk`（debug 签名，可直接侧载安装）。

## 目录结构

```
android-2026-calendar/
├── build.sh                          # 免 Gradle 打包脚本
├── settings.gradle / build.gradle    # Gradle 工程配置
├── docs/界面预览.png                 # 界面预览
├── dist/2026calendar.apk             # 可直接安装的安装包
└── app/build.gradle
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/doubao/calendar2026/
        │   ├── MainActivity.java     # 界面与交互（代码构建 UI）
        │   ├── MonthView.java        # 月视图自绘组件（滑动 / 点击）
        │   ├── YearView.java         # 年视图自绘组件（12 个迷你月历）
        │   ├── HolidayData.java      # 2026 年放假数据与查询逻辑
        │   └── Ui.java               # 配色与 dp 换算
        └── res/                      # 主题、字符串、应用图标
```

## 二次开发

- 换年份 / 换数据：只改 `HolidayData.java` 中的 `BLOCKS` 与 `MAKEUP_DAYS`。
- 换配色：只改 `Ui.java` 的颜色常量（想做深色模式可迁移到 `values-night`）。
- 加农历 / 节气：在 `HolidayData.DayInfo` 增加字段，在 `MonthView` 标签位置多画一行。

## License

MIT

---

## English Version

# 2026 Calendar (Android)

A fully native Android calendar app (Java + hand-drawn Canvas), with **zero third-party dependencies** and **no network access required**. It shows every Chinese public holiday of 2026 at a glance, with **month view / year view** switching.

### Features

- **Month view** — a 7×6 calendar grid: public holidays get a red rounded block plus the holiday name, make-up workdays are marked in blue, today is outlined. Swipe left/right to change month, tap a date to see its status.
- **Year view** — twelve mini month grids (3 columns × 4 rows) show the whole year's holidays and make-up workdays on one screen; tap any month to jump to the month view; a list below gives the full date range and length of all seven holidays.
- **Info card** — days off this month, make-up days, holiday ranges, and a countdown to the next holiday.
- **"Today" button** — jump back to the current month and select today.
- Year summary: 33 days off, 7 holidays, 6 make-up workdays.

All data follows the official *Notice of the General Office of the State Council on the Arrangement of Public Holidays in 2026* (Guobanfamingdian [2025] No. 7).

### 2026 Holiday Data

| Holiday | Dates | Days | Make-up workdays |
|---|---|---|---|
| New Year's Day | Jan 1 – Jan 3 | 3 | Jan 4 |
| Spring Festival | Feb 15 – Feb 23 | 9 | Feb 14, Feb 28 |
| Qingming Festival | Apr 4 – Apr 6 | 3 | — |
| Labour Day | May 1 – May 5 | 5 | May 9 |
| Dragon Boat Festival | Jun 19 – Jun 21 | 3 | — |
| Mid-Autumn Festival | Sep 25 – Sep 27 | 3 | — |
| National Day | Oct 1 – Oct 7 | 7 | Sep 20, Oct 10 |

### Technical Notes

- Language / framework: Java with native Android Views — no third-party libraries, no Jetpack Compose, no network permission.
- UI: built entirely in `Activity` code; the calendar grids are drawn on `Canvas` (`MonthView` / `YearView`). The APK is about 60 KB.
- Compatibility: `minSdk 21` (Android 5.0+), `targetSdk 34`.
- Data layer: `HolidayData` builds an in-memory model of all 366 days, with three day types: workday, holiday, and make-up workday.

### Build

**Option 1 — Android Studio**

Open the `android-2026-calendar/` project folder and hit Run / Build APK (AGP 8.1.4 + Gradle 8, `compileSdk 34`).

**Option 2 — Command line, no Gradle**

`build.sh` packages the app with the SDK's own `aapt2 + javac + d8 + apksigner`; no Gradle, no dependency downloads:

```bash
export ANDROID_SDK=/path/to/android-sdk   # needs platforms/android-34/android.jar and build-tools/34.0.0
./build.sh
```

Output: `app/build/outputs/apk/2026日历.apk` (debug-signed, sideload-ready).

### Project Layout

```
app/src/main/java/com/doubao/calendar2026/
├── MainActivity.java   # UI and interactions (UI built in code)
├── MonthView.java      # Month grid custom view (swipe / tap)
├── YearView.java       # Year grid custom view (12 mini months)
├── HolidayData.java    # 2026 holiday data and queries
└── Ui.java             # Colors and dp helpers
```

### Extending

- Change the year or the data: edit `BLOCKS` and `MAKEUP_DAYS` in `HolidayData.java`.
- Change the palette: edit the color constants in `Ui.java` (move them to `values-night` for dark mode).
- Add lunar dates or solar terms: add fields to `HolidayData.DayInfo` and draw one more line in `MonthView`.

### License

MIT
