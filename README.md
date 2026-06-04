# AppFitness 💪🧠

App Android per il fitness che unisce **allenamento** e **benessere emozionale**:
esercizi, timer di recupero, monitoraggio di ripetizioni/serie/peso e un diario
emozionale integrato che mostra *come l'allenamento cambia il tuo umore*.

> Stack: **Kotlin** · **Jetpack Compose (Material 3)** · **Room** · **MVVM** ·
> Navigation Compose. `minSdk 26`, `targetSdk 35`.

---

## 📊 Analisi di mercato

Il mercato delle app fitness vale ~**12,9 miliardi USD (2025)** con crescita stimata
**CAGR ~13,5%** fino al 2034. Due trend dominanti per il 2026:

1. **Benessere olistico** — le app non si limitano più ai numeri dell'allenamento ma
   integrano gestione dello stress, sonno e umore ("coaching emozionalmente
   intelligente" che adatta l'intensità allo stato dell'utente).
2. **Personalizzazione + wearable + AI** — adattamento in tempo reale su HRV e
   dati di recupero.

### Cosa fanno i principali concorrenti

| App | Punti di forza | Limite rilevante |
|-----|----------------|------------------|
| **Strong / Hevy** | Log serie/rep velocissimo, rest timer, Apple Watch, social | Nessun tracciamento emozionale |
| **Fitbod** | AI adattiva su recupero muscolare | Focus solo performance |
| **JEFIT** | Database esercizi enorme, tracking dettagliato | Interfaccia densa, nessun mood |
| **Freeletics / Nike Training Club** | Programmi guidati, coaching | Poco controllo sul log manuale |
| **Daylio / mood tracker** | Ottimo diario emozionale | Nessuna integrazione con l'allenamento |

### 🎯 Opportunità → la nostra proposta

Il mercato tratta **fitness** e **umore** come silos separati. I tracker di forza
non registrano le emozioni; le app di mood non sanno cosa hai allenato.

**AppFitness colma questo gap**: registra umore ed energia **prima e dopo** ogni
sessione e ne mostra l'andamento, trasformando la motivazione ("mi alleno perché
mi fa stare meglio") in un dato visibile. È un differenziatore allineato al trend
del benessere olistico ma con un'esecuzione semplice e offline-first.

---

## ✨ Funzionalità implementate

- **Corsa & Bici con GPS + modalità fantasma** — registra il percorso (distanza,
  tempo, ritmo) tramite GPS in un **foreground service** (traccia anche a schermo
  spento) e gareggia contro un **fantasma**: una **sessione precedente**, un
  **ritmo costante** o un **tempo obiettivo** da battere. In tempo reale mostra se
  sei *avanti* o *indietro* e di quanti metri.
  - **Percorso su mappa**: la traccia GPS è disegnata su mappa **OpenStreetMap**
    (osmdroid, senza API key) sia **dal vivo** durante l'attività sia nel
    **dettaglio** di ogni sessione salvata (con marker di partenza e arrivo).
  - **Export GPX**: condividi il percorso di qualsiasi attività in formato GPX
    standard (compatibile con Strava, Garmin, ecc.).
  - **Sistema di ricompense (dopamina)**: durante la sessione un motore di reward
    dà rinforzo immediato — **punteggio live**, **combo** sugli split più veloci,
    **bonus a sorpresa** a rapporto variabile, **traguardi** di distanza, sorpasso
    del fantasma, minuti in zona e **record personali** — con celebrazione
    **vocale + vibrazione + banner animato**.
- **Ricompense su tutte le attività + progressione**: ogni attività (allenamenti
  di forza, serie guidate, programmi, test cardio, check-in umore e sessioni GPS)
  assegna **punti** in un sistema condiviso e persistente. Ovunque guadagni punti
  appare una **celebrazione (snackbar + vibrazione)**; la Home mostra **livello,
  punti totali e progressi** verso il livello successivo.
- **Traguardi**: schermata dedicata con **streak giornaliera** 🔥, **obiettivi
  settimanali** (allenamenti, km, punti) con barre di avanzamento e **badge
  sbloccabili** (distanza, costanza, punti, serie) che si sbloccano automaticamente
  al raggiungimento delle condizioni, con celebrazione "epica".
  - **Coach vocale (corsa e bici), calibrato sulla performance**: interviene solo
    quando serve — **battito fuori dalla zona target** (in base all'età; con
    battito alto dà la **guida alla respirazione**), **molto lontano
    dall'obiettivo**, o **ritmo perso** — mentre quando va tutto bene dà un
    feedback **ogni 1 km**. Messaggi e unità si adattano all'attività: **ritmo
    (min/km) per la corsa**, **velocità (km/h) per la bici**.
- **Test di inizio** — un assessment iniziale (push-up, squat in 60s, plank)
  calcola il **livello di partenza** e il carico di base per i programmi.
- **Test cardio con cardiofrequenzimetro** — collega una fascia/orologio
  **Bluetooth LE** (Heart Rate Service standard 0x180D) e svolgi un protocollo
  guidato riposo → sforzo → recupero: l'app legge i BPM live e stima **VO₂max** e
  **recupero cardiaco (HRR)**, derivandone una valutazione della condizione fisica
  e un livello consigliato per i programmi.
- **Programmi per sport specifico** — piani multi-settimana per Pallavolo,
  Ciclismo, Fitness, Calcio, Running, Nuoto, Tennis, Basket. Ogni sport alterna
  obiettivi (forza/cardio/total body) coerenti con la disciplina.
- **Programmazione adattiva** — dopo ogni sessione del programma il carico si
  ricalibra automaticamente in base a **% serie completate + energia post-workout**:
  progressione quando vai forte, scarico quando fai fatica, con suggerimento testuale.
- **Allenamento guidato con sensori (coach vocale)** — durante una serie a
  ripetizioni, il telefono (indossato al polso) conta le **ripetizioni reali**
  tramite l'accelerometro, legge il **battito** dalla fascia cardio e fa da
  personal trainer **a voce**: incoraggia, rileva la fatica (cadenza che cala o
  battito alto) e ti spinge — *"Forza, ancora 2 ripetizioni!"* — fino a
  completare la serie, salvando il conteggio effettivo. Conteggio manuale di
  riserva se l'accelerometro non rileva.
  - **Soglie adattive per esercizio**: il rilevatore di ripetizioni si calibra
    sul tipo di movimento (forza ampio/lento, core piccolo, cardio esplosivo).
  - **Zone di frequenza cardiaca live**: mostra la zona corrente (Riposo →
    Massimale) rispetto alla zona target e **avvisa a voce** quando ne esci
    ("Aumenta il ritmo" / "Rallenta"), in base alla FCmax stimata dall'età.
- **Generatore di percorsi completi** — scegli obiettivo (forza, cardio,
  dimagrimento, mobilità, total body), livello e durata: l'app costruisce un
  allenamento completo dalla libreria, adattando serie/ripetizioni/recupero e
  **modulando l'intensità in base all'energia del giorno**. Anteprima, "rigenera"
  e avvio in un tap.
- **Libreria esercizi con immagini guida animate e filtro per attrezzo** — 24
  esercizi pre-caricati (corpo libero + **bilanciere, manubri, panca, lat machine,
  leg machine**) filtrabili per **categoria** e per **attrezzo** (vedi solo ciò che
  puoi fare con quello che hai), ciascuno con un'**anteprima animata a 2 frame**
  (posizione iniziale ⇄ finale) che suggerisce il movimento; immagini di pubblico
  dominio da [free-exercise-db](https://github.com/yuhonas/free-exercise-db), bundle
  offline. Creazione di esercizi personalizzati con scelta dell'attrezzo.
- **Sessione di allenamento** con:
  - **Cronometro** della sessione sempre visibile.
  - **Timer di recupero** automatico tra le serie (con +15s / −15s / salta).
  - **Tracciamento ripetizioni, peso (kg) e durata** per ogni serie, con stepper.
  - Spunta di completamento serie che avvia il recupero.
- **Check-in emozionale** prima (umore + energia) e dopo (umore + energia + note)
  ogni allenamento.
- **Diario emozionale** indipendente con tag, note e mini-grafico dell'andamento.
- **Storico** dettagliato: durata, volume (kg), serie, transizione umore `prima → dopo`.
- **Home/dashboard** con statistiche settimanali (allenamenti, minuti, umore medio).
- Material 3 con **tema dinamico** (Material You) e supporto dark mode.

## 🏗️ Architettura

```
app/src/main/java/com/appfitness/app/
├── data/            # Room: entity, dao, repository, seed, container DI
│   ├── entity/      # Exercise, WorkoutSession, SetLog, MoodEntry
│   ├── dao/         # ExerciseDao, WorkoutDao, MoodDao
│   ├── relation/    # SessionWithSets
│   └── model/       # ExerciseCategory, MoodLevel
└── ui/
    ├── home/        # dashboard + avvio allenamento
    ├── workout/     # sessione attiva, timer, picker esercizi
    ├── exercises/   # libreria + creazione custom
    ├── mood/        # diario emozionale
    ├── history/     # storico allenamenti
    ├── components/  # MoodSelector riusabile
    └── theme/       # Material 3
```

Pattern **MVVM**: le `Composable` osservano gli `StateFlow` dei `ViewModel`, che
parlano solo con `FitnessRepository` (unica fonte di verità sopra i DAO Room).
Dependency injection manuale leggera via `AppContainer`.

## 🚀 Come compilare

Requisiti: Android Studio (Ladybug+) oppure JDK 17 + Android SDK 35.

```bash
./gradlew :app:assembleDebug      # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug            # installa su device/emulatore collegato
```

In alternativa apri la cartella in Android Studio e premi ▶︎.

## 🔭 Roadmap (prossimi passi proposti)

- Programmi/routine multi-settimana e suggerimenti adattivi (trend AI).
- Integrazione wearable (Health Connect) per HR e calorie.
- Correlazione automatica umore↔allenamento con insight ("ti alleni meglio il mattino").
- Notifiche promemoria e widget.
- Esportazione dati e backup cloud.
