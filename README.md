# PaySound v2

UPI / bank payment aate hi phone awaaz me bolta hai: "500 रुपये प्राप्त हुए".
Poori tarah free, koi ads ya subscription nahi.

## Kya naya hai (v2)
- Fixed signing key wala release APK (har build same signature, update install hota hai)
- Naya app id: `com.techx.paysound` (purane PaySound se conflict nahi)
- App icon, payment history, aaj ka total
- Test box: koi bhi message paste karke dekho parse hota hai ya nahi
- Awaaz ki speed, "sabhi apps" option
- "request", "failed", "pending", "OTP" wale messages ignore

## Kaise kaam karta hai
- Android ka Notification Listener GPay, PhonePe, Paytm, BHIM, Amazon Pay aur SMS apps ke notifications padhta hai.
- "received / credited / paid you" jaise words aur amount (₹ / Rs) pakadta hai.
- Debit wale notifications ignore hote hain.
- Phone ka Text-to-Speech Hindi ya English me bolta hai.

## Update push karna (Termux)
```bash
cd ~/storage/downloads/PaySound
git add .
git commit -m "PaySound v2"
git push
```
Build ke baad `github.com/techXsollutions/soundbox/releases/tag/latest` se `PaySound.apk` download karke install karo.

## Pehli baar setup
1. App kholo, **Notification access do** dabao aur PaySound ON karo.
   Na ho to Settings > Apps > PaySound > teen dots > **Allow restricted settings**.
2. **Battery optimization hatao** me PaySound ko "Don't optimize" karo.
3. OnePlus: Settings > Battery > App battery management me PaySound ko "Allow background activity" do.
4. Test box me message daalkar bolwa ke dekho.

## Note
Signing keystore (`app/paysound.keystore`) repo me hi hai taaki build har baar same signature se ho. Ye sirf personal sideload ke liye theek hai.

## Customize
Aur apps ke notifications chahiye to `PaymentParser.kt` ki `allowedPackages` list me package name jodo.
