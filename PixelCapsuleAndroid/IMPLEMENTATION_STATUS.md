# Stav implementácie

| Oblasť | Stav zdrojového kódu | Poznámka |
|---|---|---|
| Samostatná natívna Android aplikácia | Implementované | Kotlin / Compose, vlastný názov a ikona |
| Material 3 a dynamické farby | Implementované | Dynamic color Android 12+, vlastná fallback paleta |
| Overlay kapsula | Implementované | TYPE_APPLICATION_OVERLAY, animované rozbalenie, collapse, gestá |
| Automatické zarovnanie ku punch-hole | Čiastočne | Používa verejný DisplayCutout/WindowMetrics; systémový status bar je nad aplikačným overlayom |
| MediaSession prehrávač | Implementované s podmienkou | Vyžaduje povolený Notification Listener a zdrojovú MediaSession |
| Notification Listener | Implementované | Zobrazuje posledné upozornenia v pamäti procesu |
| Quick Reply | Implementované s podmienkou | Len ak notifikácia ponúka RemoteInput |
| Baterka a intenzita | Implementované s podmienkou | CAMERA permission; intenzita iba na podporovanom hardvéri od API 33 |
| Jas / automatický jas | Implementované | WRITE_SETTINGS user grant |
| Hlasitosť médií/zvonenia | Implementované | AudioManager |
| SIM hovory | Čiastočne | TelephonyCallback + TelecomManager; VoIP nie je univerzálne |
| Batéria a nabíjanie | Implementované | ACTION_BATTERY_CHANGED |
| Bluetooth pripojenie | Čiastočne | Connected/disconnected broadcast; universal accessory battery percent unavailable |
| Aplikácie | Implementované | User-selected launcher apps; nie skryté balíky |
| Sťahovania | Čiastočne | Progress len ak je zverejnený v notifikácii |
| Kontakty / kontaktové skratky | Neimplementované | Zatiaľ iba aplikácie a systémové ovládače |
| Vlastný výber farieb | Čiastočne | Dynamické farby Material You a predvolená paleta; bez vlastného farebného editora |
| Editor vlastných gest | Neimplementované | Zatiaľ sú prednastavené tap/double-tap/long-press/swipe akcie |
| Ekvalizér podľa signálu | Čiastočne | Animovaný vizuálny indikátor; nie je napojený na audio capture |
| AccessibilityService | Zámerne vynechané | Nepotrebné pre vlastný overlay a vlastné gestures |

## Validácia

Nebolo možné spustiť Gradle Sync/assemble, lebo v pracovnom kontajneri nie je Android SDK, Gradle ani Android platformové JAR a sieťový DNS prístup je vypnutý. Žiadne tvrdenie o úspešnej kompilácii alebo bezchybnom APK sa preto nerobí.
