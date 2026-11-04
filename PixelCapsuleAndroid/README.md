# Pixel Capsule — natívny Android projekt

Samostatná Android aplikácia v Kotlin + Jetpack Compose. Projekt je nezávislá natívna aplikácia bez webového wrappera. Dizajn používa Material 3, systémové dynamické farby na Android 12+ a vlastnú zeleno-krémovú paletu ako zálohu.

## Obsah

- Plávajúca kapsula cez `TYPE_APPLICATION_OVERLAY`, s rozšíreným panelom, animovanou zmenou rozmerov, gestami a nastaveniami polohy/šírky/zaoblenia.
- Detekcia polohy výrezu displeja cez `DisplayCutout` a `WindowMetrics`; používateľ môže doladiť posun X/Y.
- Hudobné ovládanie cez aktívne `MediaSession`: obal albumu, skladba, interpret, seek, play/pause, ďalšia a predchádzajúca skladba.
- Notifikačný panel z `NotificationListenerService`; rýchla odpoveď sa odošle iba vtedy, keď zdrojová notifikácia poskytuje `RemoteInput` akciu.
- Ovládanie LED blesku, manuálneho/automatického jasu a hlasitosti médií/zvonenia.
- Stav SIM hovoru, časovač hovoru a dostupné prijatie/odmietnutie cez `TelecomManager`.
- Stav batérie a pripojenia Bluetooth; výber obľúbených spustiteľných aplikácií.
- Lokálne nastavenia uložené v `SharedPreferences`. Obsah notifikácií sa neukladá na disk ani neposiela na server.

## Zostavenie v Android Studio

1. Otvor priečinok `PixelCapsuleAndroid` v aktuálnom Android Studio.
2. Použi JDK 17 a Gradle 9.5.0 (požiadavka AGP 9.3.1).
3. Nainštaluj Android SDK Platform 36 a SDK Build Tools 36.0.0.
4. Synchronizuj Gradle a spusti `app` na zariadení alebo emulátore s Androidom 8.0+.
5. APK zostavíš cez **Build → Build Bundle(s) / APK(s) → Build APK(s)**. Pri príkazovom riadku použi `gradle :app:assembleDebug`, keď máš nainštalovaný Gradle 9.5.0.

Zostavený debug APK bude v `app/build/outputs/apk/debug/app-debug.apk`.

## Prvé spustenie

V aplikácii povoľ podľa potreby:

- zobrazenie nad ostatnými aplikáciami,
- prístup k notifikáciám (potrebný aj na čítanie aktívnych MediaSession),
- úpravu systémových nastavení (jas),
- oprávnenia Telefón a Fotoaparát (stav hovoru a baterka).

Povolenia udeľuj iba tým funkciám, ktoré chceš používať. Prístup k notifikáciám môže odhaľovať citlivý text správ; spracovanie v tejto aplikácii je lokálne.

## Čo ešte nie je hotové

Tento projekt je funkčný základ natívnej aplikácie, nie dokončený komerčný produkt. Zatiaľ neobsahuje výber vlastných farieb (okrem dynamických farieb Material You a predvolenej palety), editor vlastných akcií gest, skratky na kontakty ani univerzálny stav batérie každého Bluetooth príslušenstva. Ekvalizér je animovaný vizuálny indikátor, nie analýza reálneho zvukového signálu. Pred vydaním treba doplniť tieto časti podľa cieľových zariadení, otestovať oprávnenia a správanie na rôznych výrobcoch Androidu.

## Technické poznámky a limity Androidu

- Aplikačný overlay je systémom umiestnený pod kritickými systémovými oknami, napríklad stavovým riadkom. Aplikácia nemôže nahradiť systémový status bar ani garantovať prekrytie všetkých prvkov výrobcu. Zarovnanie ku kamera výrezu je najlepšie možné podľa verejného `DisplayCutout` API.
- Media ovládanie závisí od toho, či prehrávajúca aplikácia publikuje ovládateľnú `MediaSession`.
- Rýchla odpoveď funguje len pre notifikácie, ktorých aplikácia ponúka `RemoteInput`; nie všetky Messenger/WhatsApp/SMS notifikácie takú akciu poskytujú.
- Informácie o sťahovaní sa zobrazia iba v prípade, že zdrojová aplikácia zverejní štandardné progress extras v notifikácii. Nie je možné univerzálne čítať interný stav každého sťahovania.
- Zmena sily blesku je možná len na zariadeniach s podporou úrovní intenzity (Android 13+); inak sa použije obyčajné zap/vyp.
- Jas vyžaduje osobitné schválenie `WRITE_SETTINGS`. Posuvník manuálneho jasu prepne automatický režim na manuálny; automatický jas sa dá znovu zapnúť samostatným tlačidlom.
- Android obmedzuje ovládanie telefónnych hovorov a niektoré `TelecomManager` metódy sú od API 29 zastarané; výsledok závisí od typu hovoru a systému. Bežné VoIP hovory nemusia poskytovať stav cez telephony callback.
- Bluetooth príslušenstvo zobrazuje stav pripojenia. Univerzálne percento batérie všetkých slúchadiel/hodiniek nie je dostupné cez jedno verejné API pre všetkých výrobcov.
- Zoznam aplikácií obsahuje spustiteľné aplikácie dostupné cez launcher intent; aplikácia nežiada obmedzené `QUERY_ALL_PACKAGES`.
- AccessibilityService sa zámerne nepoužíva: kreslenie overlay okna a spracovanie vlastných dotykových gest nevyžaduje prístup k celej obrazovke.

## Stav zostavenia

Tento zdrojový projekt bol vytvorený v prostredí bez nainštalovaného Android SDK/Gradle a bez možnosti stiahnuť chýbajúce nástroje. Preto tu nebolo možné vytvoriť ani overiť APK. Projekt je pripravený na otvorenie a zostavenie v Android Studio, ale pred vydaním treba urobiť Gradle sync, kompiláciu a testy na fyzickom zariadení.
