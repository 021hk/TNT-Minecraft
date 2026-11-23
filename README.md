# TNT Minecraft

<div dir="rtl">

**۹ تا TNT → ۱ بلاک. هر لِوِل دقیقاً ۹ برابر قوی‌تر از قبل — تا لِوِل ۱۰ (حدود ۳.۵ میلیارد برابر TNT معمولی)!**

ماد «TNT فشرده» برای ماینکرافت **Java Edition 1.21.1** با لودر **Fabric**:
۹ عدد TNT را کنار هم بگذار تا یک بلاک فشرده ساخته شود؛ بعد همان را دوباره
فشرده کن و تا ۱۰ لِوِل جلو برو. رنگ بلاک‌ها با بالا رفتن لِوِل از قرمز به سفید
تغییر می‌کند و **عددِ لِوِل هم روی اسمِ آیتم و هم درشت روی عکسِ بلاک** نوشته شده است.

![پیش‌نمایش TNT Minecraft](docs/preview.png)

## لِوِل‌ها و قدرت انفجار

| لِوِل | نام | ID | ضریب نسبت به TNT | شعاع تخریب |
|:---:|:---|:---|:---:|:---:|
| 1 | Compressed TNT 1 | `compressedtnt:compressed_tnt` | 9x | 8 |
| 2 | Compressed TNT 2 | `compressedtnt:double_compressed_tnt` | 81x | 16 |
| 3 | Compressed TNT 3 | `compressedtnt:triple_compressed_tnt` | 729x | 24 |
| 4 | Compressed TNT 4 | `compressedtnt:quadruple_compressed_tnt` | 6,561x | 32 |
| 5 | Compressed TNT 5 | `compressedtnt:quintuple_compressed_tnt` | 59,049x | 40 |
| 6 | Compressed TNT 6 | `compressedtnt:sextuple_compressed_tnt` | 531,441x | 48 |
| 7 | Compressed TNT 7 | `compressedtnt:septuple_compressed_tnt` | ~4.8M x | 56 |
| 8 | Compressed TNT 8 | `compressedtnt:octuple_compressed_tnt` | ~43M x | 64 |
| 9 | Compressed TNT 9 | `compressedtnt:nonuple_compressed_tnt` | ~387M x | 72 |
| 10 | Compressed TNT 10 | `compressedtnt:decuple_compressed_tnt` | **~3.49 میلیارد x** | 80 |

- پاور هر لِوِل دقیقاً **۹ برابر** لِوِل قبل است و شعاع تخریب هم درست **۸ بلوک**
  بیشتر از لِوِل قبل می‌شود؛ پس هر لِوِل کاملاً از قبلی متمایز است
  (لِوِل ۲ = شعاع ۱۶، نه بیشتر!).
- بلاک‌های پایه (Bedrock / Barrier) و چانک‌های لودنشده هرگز خراب نمی‌شوند.

## تایمر انفجار (۱ تا ۳۶۰۰ ثانیه)

1. **با کیبورد (راه اصلی):** ماد را در دست بگیر و کلید **`G`** را بزن →
   پنجره‌ای باز می‌شود → عدد ثانیه را تایپ کن (مثلاً `120` یا `1`) و
   **Enter** بزن → حالا هر جا بلاک را بگذاری، شمارش همان لحظه شروع می‌شود
   (دود بلند می‌شود = فعال است) و دقیقاً بعد از همان ثانیه‌ها منفجر می‌شود.
   کلید را می‌توانی از Options → Controls → «TNT Minecraft» عوض کنی.
2. **با ساعت (Clock):** روی بلاکِ گذاشته‌شده با ساعت راست‌کلیک کن تا شمارش
   شروع شود؛ هر کلیک `+۱` ثانیه، با Shift `−۱` ثانیه (در ۱ ثانیه، شیفت = لغو تایمر).
3. **اگر تایمری تنظیم نشود:** بلاک مثل TNT معمولی رفتار می‌کند — فقط با
   ردستون/آتش/چاقو سنگ‌چخماق روشن می‌شود و با فیوز عادی ۴ ثانیه‌ای منفجر می‌شود.

## ویژگی‌ها

- **موج هسته‌ای کروی:** انفجار به‌صورت موجی حلقوی و پله‌پله اجرا می‌شود؛ صدا،
  ذرات و پرتاب موجودیت‌ها دارد و روی سرور هم بدون فریز کار می‌کند
  (موج در هر تیک حداکثر ۵۰ هزار بلاک خراب می‌کند).
- **فیزیک کامل:** آب و گدازه بعد از انفجار به درون گودال جریان پیدا می‌کنند؛
  شن و ماسه سقوط می‌کنند و ردستون‌ها آپدیت می‌شوند.
- **بدون بلاک باقی‌مانده:** هندسه‌ی حلقوی موج اصلاح شده و دیگر هیچ بلوکی داخل
  کره‌ی انفجار باقی نمی‌ماند.
- **همه‌چیز قابل حفری:** بلاک‌های TNT فشرده با دست/کلنگ به‌راحتی شکسته می‌شوند
  و خودشان به‌عنوان آیتم می‌افتند.
- **زنجیره‌ای:** اگر موج به TNT معمولی یا TNT فشرده‌ی دیگر برسد، آن‌ها هم
  منفجر می‌شوند.
- **دستور ساخت دوطرفه:** ۹→۱ (فشرده‌سازی) و ۱→۹ (بازکردن) هر دو فعال است.
- **دو زبانه:** نام آیتم‌ها هم انگلیسی است و هم فارسی (`fa_ir`).
- اسمورتیپ با نمایش پاور، تایمر و راهنما.

## نصب (TLauncher)

۱. فایل **`TNT-Minecraft-TLauncher-1.2.0.zip`** را از صفحه‌ی
   [Releases](https://github.com/021hk/TNT-Minecraft/releases) دانلود کنید
   (داخل آن، خود ماد + Fabric API هر دو هست).
۲. در TLauncher یک بار نسخه‌ی **Fabric 1.21.1** را اجرا کنید تا پوشه‌ی
   `.minecraft` آماده شود.
۳. زیپ را Extract کنید و `install-windows.bat` را دابل‌کلیک کنید
   (مک/لینوکس: `sh install-mac-linux.sh`).
۴. در TLauncher نسخه‌ی **Fabric 1.21.1** را انتخاب و بازی کنید.

## نصب (سینگل‌پلیر / سرور معمولی)

فایل `TNT-Minecraft-1.2.0+MC1.21.1-Fabric.jar` و [Fabric API
0.116.18+1.21.1](https://modrinth.com/mod/fabric-api) را در پوشه‌ی `mods`
بگذارید (سینگل‌پلیر یا سرور). به Fabric Loader `>=0.16` و جاوا ۲۱ نیاز دارد.

## نصب روی Aternos

Aternos اجازه‌ی آپلود ماد اختصاصی را نمی‌دهد و فقط مادهای موجود در کتابخانه‌ی
خودش (Modrinth / CurseForge) را نصب می‌کند. برای همین باید اول ماد را در
Modrinth منتشر کنید؛ بعد در Aternos قابل نصب می‌شود:

1. زیپ **`TNT-Minecraft-Modrinth-Upload-Package.zip`** را دانلود کنید؛ داخل آن
   jar ماد + `description.md` (توضیح آماده‌ی صفحه) + آیکون + راهنمای ۵ قدمی
   انتشار به فارسی (`UPLOAD-GUIDE-fa.txt`) هست.
2. در [modrinth.com](https://modrinth.com) با حساب گیت‌هاب وارد شوید و ماد را
   با پروژه‌ی `TNT-Minecraft` منتشر کنید (طبق راهنما).
3. بعد از تأیید/این dex شدن ماد در Modrinth: در Aternos سرور بسازید،
   نرم‌افزار **Fabric** + نسخه **1.21.1**، سپس از بخش **Mods** عبارت
   `TNT Minecraft` را جستجو و نصب کنید؛ **Fabric API** را هم نصب کنید.
4. استارت! (تا تأیید Modrinth می‌تواند کمی طول بکشد؛ جایگزین سریع‌تر:
   exaroton که آپلود مستقیم دارد، یا Open to LAN روی سیستم خودتان.)

## بیلد از سورس

```bash
./gradlew build
# خروجی: build/libs/TNT-Minecraft-1.2.0+MC1.21.1-Fabric.jar  (نیاز به JDK 21)
```

توجه: به‌خاطر محدودیت دسترسی توکن، فایل CI در مسیر
[`docs/workflows/build.yml`](docs/workflows/build.yml) قرار دارد؛ اگر می‌خواهید
GitHub Actions خودکار بیلد بگیرد، آن را به `.github/workflows/build.yml`
منتقل کنید.

## تست‌شده روی سرور واقعی (۸ تست، همه PASS)

ماد روی یک سرور واقعی Fabric 1.21.1 با دیتاپک
[`testdata/`](testdata/) به‌صورت خودکار تست شده است:

- `CHECK_RADIUS_TIERS` — شعاع هر لِوِل دقیق است (۸ / ۱۶ / ۲۴ / ... / ۸۰)
- `CHECK_NO_LEFTOVERS` — هیچ بلوکی داخل کره‌ی انفجار باقی نمی‌ماند
- `CHECK_WATER_FLOW` — آب بعد از انفجار به درون گودال جریان پیدا می‌کند
- `CHECK_DROPS` — شکستن بلاک، آیتم را می‌اندازد (قابل حفری)
- `CHECK_FUSE_AUTO` — تایمر تنظیم‌شده دقیقاً سر وقت منفجر می‌شود
- `CHECK_CONTROL_NORMAL` — بدون تایمر، رفتار عادی وانیلا
- `CHECK_T10_RADIUS` — لِوِل ۱۰ دقیقاً شعاع ۸۰
- `CHECK_ENTITIES_CLEAN` — بعد از موج، هیچ موجودیتی جا نمی‌ماند

## تغییرات نسخه ۱.۲.۰

- **تایمر جدید با تایپ کیبورد:** کلید `G` در حالت در دست گرفتن ماد → پنجره‌ی
  نوشتن ثانیه (۱ تا ۳۶۰۰) → با جاگذاری بلاک، شمارش خودکار شروع می‌شود.
  (سیستم قدیمی `+`/`-` حذف شد چون گیج‌کننده بود.)
- **بالانس:** شعاع تخریب جدید = ۸ بلوک در هر لِوِل (لِوِل ۱ = ۸ ... لِوِل ۱۰ = ۸۰)؛
  لِوِل ۲ حالا واقعاً ضعیف‌تر و متمایز است. پاور همچنان دقیقاً ۹ برابر در هر لِوِل.
- **عدد لِوِل** روی اسم و روی تکسچر همه‌ی بلاک‌ها درشت‌تر نوشته شده.
- **رفع باگ بلاک‌های باقی‌مانده:** یک لایه‌ی مرزی در هندسه‌ی موج جا می‌ماند؛
  اصلاح شد و الان هیچ بلوکی داخل کره باقی نمی‌ماند.
- **رفع باگ فیزیک:** بلاک‌ها با فلگ‌های درست حذف می‌شوند؛ آب/گدازه جریان
  می‌یابند، شن می‌ریزد.
- بلاک TNT فشرده به‌راحتی قابل شکستن است و آیتم می‌اندازد.

</div>

---

**TNT Minecraft** — a Fabric mod for Minecraft **Java 1.21.1**: combine 9 TNT
into 1 block, then compress again, tier by tier (10 tiers). Each tier is exactly
**9x stronger** than the previous one — the final tier is about **3.49 billion
times** a normal TNT. Tier names and textures carry their tier number. Set the
fuse with the **G key** while holding the item (type seconds 1-3600; the
countdown starts the moment you place the block) or with a **Clock** on the
placed block; with no timer set the block behaves like vanilla TNT (4 s fuse).
Destruction radius is 8 + 8x(tier-1) blocks (tier 10 = 80). Fluids flow back
into the crater, no leftover blocks, everything stays mineable, and crafting
works both ways (9 to 1 and 1 to 9).

## License

MIT — see [LICENSE](LICENSE).
