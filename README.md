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

- **Libreria esercizi** — 12 esercizi pre-caricati (forza, cardio, core, mobilità,
  total body) filtrabili per categoria + creazione di esercizi personalizzati.
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
