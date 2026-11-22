# TNT Minecraft

<div dir="rtl">

**۹ تا TNT → ۱ بلاک. هر لِوِل، ۹ برابر قوی‌تر از قبل — تا لِوِل ۱۰!**

ماد «TNT فشرده» برای ماینکرافت **Java Edition 1.21.1** با لودر **Fabric**:
۹ عدد TNT را کنار هم بگذار تا یک بلاک فشرده ساخته شود؛ بعد همان را دوباره
فشرده کن و تا ۱۰ لِوِل جلو برو. رنگ بلاک‌ها با بالا رفتن لِوِل از قرمز به سفید
تغییر می‌کند و پاور آن‌ها به‌صورت نمایی (۹ برابر در هر لِوِل) زیاد می‌شود.

![پیش‌نمایش TNT Minecraft](docs/preview.png)

## لِوِل‌ها و قدرت انفجار

| لِوِل | نام | ID | ضریب نسبت به TNT | شعاع تخریب |
|:---:|:---|:---|:---:|:---:|
| 1 | Compressed TNT | `compressedtnt:compressed_tnt` | 9x | 40 |
| 2 | Double Compressed TNT | `compressedtnt:double_compressed_tnt` | 81x | 48 |
| 3 | Triple Compressed TNT | `compressedtnt:triple_compressed_tnt` | 729x | 56 |
| 4 | Quadruple Compressed TNT | `compressedtnt:quadruple_compressed_tnt` | 6,561x | 64 |
| 5 | Quintuple Compressed TNT | `compressedtnt:quintuple_compressed_tnt` | 59,049x | 64 |
| 6 | Sextuple Compressed TNT | `compressedtnt:sextuple_compressed_tnt` | 531,441x | 64 |
| 7 | Septuple Compressed TNT | `compressedtnt:septuple_compressed_tnt` | ~4.8M x | 64 |
| 8 | Octuple Compressed TNT | `compressedtnt:octuple_compressed_tnt` | ~43M x | 64 |
| 9 | Nonuple Compressed TNT | `compressedtnt:nonuple_compressed_tnt` | ~387M x | 64 |
| 10 | Decuple Compressed TNT | `compressedtnt:decuple_compressed_tnt` | **~3.49 میلیارد x** | 64 |

- شعاع تخریب بعد از لِوِل ۴ روی **۶۴ بلاک** ثابت می‌ماند تا بازی همیشه پایدار
  بماند (بعد از آن فقط عدد پاور و خشنی موج رشد می‌کند).
- بلاک‌های پایه (Bedrock / Barrier) و چانک‌های لودنشده هرگز خراب نمی‌شوند.

## ویژگی‌ها

- **موج هسته‌ای کروی:** به‌جای یک انفجار معمولی، انفجار به‌صورت موجی حلقوی
  و پله‌پله اجرا می‌شود؛ صدا، ذرات و لرزش دارد و روی سرور هم بدون فریز کار می‌کند.
- **زنجیره‌ای:** اگر موج به TNT معمولی یا TNT فشرده‌ی دیگر برسد، آن‌ها هم
  منفجر می‌شوند (تست شده؛ بدون لگ).
- **فتیله قابل تنظیم:** روی بلاکِ درون‌جیبی با **ساعت (Clock) راست‌کلیک**
  کنید تا فتیله +۱ ثانیه شود و با **Shift + راست‌کلیک** -۱ ثانیه
  (از ۱ تا ۳۰ ثانیه؛ پیش‌فرض ۴ ثانیه).
- **دستور ساخت دوطرفه:** ۹→۱ (فشرده‌سازی) و ۱→۹ (بازکردن) هر دو فعال است.
- **دو زبانه:** نام آیتم‌ها هم انگلیسی است و هم فارسی (`fa_ir`).
- اسمورتیپ با نمایش پاور و راهنمای فتیله.

## نصب (TLauncher)

۱. فایل **`TNT-Minecraft-TLauncher-1.0.0.zip`** را از صفحه‌ی
   [Releases](https://github.com/021hk/TNT-Minecraft/releases) دانلود کنید
   (داخل آن، خود ماد + Fabric API هر دو هست).
۲. در TLauncher یک بار نسخه‌ی **Fabric 1.21.1** را اجرا کنید تا پوشه‌ی
   `.minecraft` آماده شود.
۳. `install-windows.bat` را دابل‌کلیک کنید (مک/لینوکس: `sh install-mac-linux.sh`).
۴. در TLauncher نسخه‌ی **Fabric 1.21.1** را انتخاب و بازی کنید.

## نصب (سرویس‌های Fabric / CurseForge-style)

فایل `TNT-Minecraft-1.0.0+MC1.21.1-Fabric.jar` و [Fabric API
0.116.18+1.21.1](https://modrinth.com/mod/fabric-api) را در پوشه‌ی `mods`
بگذارید (سینگل‌پلیر یا سرور). به Fabric Loader `>=0.16` و جاوا ۲۱ نیاز دارد.

## نصب روی سرور (Aternos و ...)

1. در پنل سرور، نرم‌افزار **Fabric** و نسخه **1.21.1** را انتخاب کنید.
2. ماد را از بخش Mods/Files آپلود کنید و **Fabric API** را هم نصب کنید.
3. سرور را استارت بزنید. برای تست سریع می‌توانید این دستورها را بزنید:

```
/give @s compressedtnt:decuple_compressed_tnt
/fill ~2 ~ ~ ~5 ~2 ~ redstone_block
```

(بلاک را نزدیک `redstone_block` بیندازید تا منفجر شود.)

## بیلد از سورس

```bash
./gradlew build
# خروجی: build/libs/TNT-Minecraft-1.0.0.jar  (نیاز به JDK 21)
```

توجه: به‌خاطر محدودیت دسترسی توکن، فایل CI در مسیر
[`docs/workflows/build.yml`](docs/workflows/build.yml) قرار دارد؛ اگر می‌خواهید
GitHub Actions خودکار بیلد بگیرد، آن را به `.github/workflows/build.yml`
منتقل کنید.

## تست‌شده روی سرور واقعی

این ماد روی یک سرور واقعی Fabric 1.21.1 به‌صورت خودکار تست شده است:
بلوک TNT فشرده با `redstone_block` منفجر شده، موج کروی زمین را به شکل گودال
کره‌ای حفر کرده و انفجار زنجیره‌ای TNTها درست اجرا شده است
(`CHECK_REDSTONE_DESTROYED`، `CHECK_RING_DESTROYED`، `CHECK_TNT_GONE` —
هر سه PASS). دیتاپک تست هم در پوشه‌ی [`testdata/`](testdata/) موجود است.

</div>

---

**TNT Minecraft** — a Fabric mod for Minecraft **Java 1.21.1**: combine 9 TNT
into 1 block, then compress again, tier by tier (10 tiers). Each tier is 9x
stronger than the previous one — the final tier is about **3.49 billion times**
a normal TNT. High tiers detonate as a stable nuke-style spherical shockwave
(destroy radius capped at 64), the fuse timer is adjustable 1-30s with a
**Clock**, and crafting works both ways (9 to 1 and 1 to 9).

## License

MIT — see [LICENSE](LICENSE).
