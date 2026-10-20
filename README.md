# PaySound

UPI / bank payment aate hi phone awaaz me bolta hai: "500 रुपये प्राप्त हुए".
Poori tarah free, koi ads ya subscription nahi.

## Kaise kaam karta hai
- Android ka Notification Listener GPay, PhonePe, Paytm, BHIM, Amazon Pay, WhatsApp Pay aur SMS apps ke notifications padhta hai.
- "received / credited / paid you" jaise words aur amount (₹ / Rs) pakadta hai.
- Debit wale notifications ignore hote hain.
- Phone ka Text-to-Speech Hindi ya English me bolta hai.

## Termux se GitHub par push
```bash
pkg install git gh
gh auth login
cd PaySound
git init
git add .
git commit -m "PaySound first version"
git branch -M main
gh repo create PaySound --public --source=. --push
```

Push hote hi GitHub Actions APK banata hai (2-4 minute). Phir:
`github.com/<tumhara-username>/PaySound/releases/tag/latest` se `PaySound.apk` download karke install karo.

## Pehli baar setup
1. App kholo, **Notification access do** dabao aur PaySound ON karo.
2. **Battery optimization hatao** me PaySound ko "Don't optimize" karo.
3. Phone ke Settings me Auto-start / background restriction ho (Xiaomi, Oppo, Vivo, Realme) to PaySound ko allow karo.
4. **Test awaaz** dabao.

## Customize
Aur apps ke notifications chahiye to `PaymentParser.kt` ki `allowedPackages` list me package name jodo.
