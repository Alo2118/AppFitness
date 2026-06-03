package com.appfitness.app.data

import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.model.ExerciseCategory

/**
 * Starter exercise library seeded on first launch so the app is useful out of
 * the box. Users can add their own custom exercises on top of these.
 */
object ExerciseSeed {

    val exercises: List<Exercise> = listOf(
        Exercise(
            name = "Squat a corpo libero",
            category = ExerciseCategory.STRENGTH,
            muscleGroup = "Gambe, glutei",
            description = "Piedi alla larghezza delle spalle, scendi spingendo i fianchi indietro mantenendo la schiena dritta.",
            isTimeBased = false, defaultSets = 4, defaultReps = 15, defaultRestSec = 60,
            imageAsset = "exercises/squat.jpg",
        ),
        Exercise(
            name = "Affondi alternati",
            category = ExerciseCategory.STRENGTH,
            muscleGroup = "Gambe, glutei",
            description = "Passo avanti, piega entrambe le ginocchia a 90°, torna in posizione e alterna gamba.",
            isTimeBased = false, defaultSets = 3, defaultReps = 12, defaultRestSec = 60,
            imageAsset = "exercises/lunge.jpg",
        ),
        Exercise(
            name = "Push-up",
            category = ExerciseCategory.STRENGTH,
            muscleGroup = "Petto, tricipiti, spalle",
            description = "Corpo in linea retta, scendi piegando i gomiti e spingi verso l'alto.",
            isTimeBased = false, defaultSets = 4, defaultReps = 12, defaultRestSec = 75,
            imageAsset = "exercises/pushup.jpg",
        ),
        Exercise(
            name = "Plank",
            category = ExerciseCategory.CORE,
            muscleGroup = "Core, addome",
            description = "Avambracci a terra, corpo in linea retta, contrai addome e glutei.",
            isTimeBased = true, defaultSets = 3, defaultDurationSec = 45, defaultRestSec = 45,
            imageAsset = "exercises/plank.jpg",
        ),
        Exercise(
            name = "Mountain climber",
            category = ExerciseCategory.CARDIO,
            muscleGroup = "Core, cardio",
            description = "In posizione di plank alta, porta velocemente le ginocchia al petto alternandole.",
            isTimeBased = true, defaultSets = 4, defaultDurationSec = 30, defaultRestSec = 30,
            imageAsset = "exercises/mountain_climber.jpg",
        ),
        Exercise(
            name = "Jumping jack",
            category = ExerciseCategory.CARDIO,
            muscleGroup = "Total body, cardio",
            description = "Salta aprendo gambe e braccia, poi richiudi. Mantieni un ritmo costante.",
            isTimeBased = true, defaultSets = 3, defaultDurationSec = 40, defaultRestSec = 30,
            imageAsset = "exercises/jumping_jack.jpg",
        ),
        Exercise(
            name = "Burpees",
            category = ExerciseCategory.FULL_BODY,
            muscleGroup = "Total body",
            description = "Squat, kick-back in plank, push-up, ritorno e salto esplosivo verso l'alto.",
            isTimeBased = false, defaultSets = 4, defaultReps = 10, defaultRestSec = 75,
            imageAsset = "exercises/burpee.jpg",
        ),
        Exercise(
            name = "Crunch addominali",
            category = ExerciseCategory.CORE,
            muscleGroup = "Addome",
            description = "Sdraiato, solleva le scapole contraendo l'addome senza tirare il collo.",
            isTimeBased = false, defaultSets = 3, defaultReps = 20, defaultRestSec = 45,
            imageAsset = "exercises/crunch.jpg",
        ),
        Exercise(
            name = "Stacco rumeno con manubri",
            category = ExerciseCategory.STRENGTH,
            muscleGroup = "Femorali, glutei, schiena",
            description = "Manubri davanti alle cosce, fianchi indietro, scendi mantenendo la schiena neutra.",
            isTimeBased = false, defaultSets = 4, defaultReps = 10, defaultRestSec = 90,
            imageAsset = "exercises/romanian_deadlift.jpg",
        ),
        Exercise(
            name = "Corsa sul posto",
            category = ExerciseCategory.CARDIO,
            muscleGroup = "Cardio, gambe",
            description = "Corri sul posto portando le ginocchia in alto, mantenendo un ritmo sostenuto.",
            isTimeBased = true, defaultSets = 3, defaultDurationSec = 60, defaultRestSec = 30,
            imageAsset = "exercises/running.jpg",
        ),
        Exercise(
            name = "Superman",
            category = ExerciseCategory.MOBILITY,
            muscleGroup = "Lombari, schiena",
            description = "Prono, solleva contemporaneamente braccia e gambe contraendo la schiena bassa.",
            isTimeBased = true, defaultSets = 3, defaultDurationSec = 30, defaultRestSec = 40,
            imageAsset = "exercises/superman.jpg",
        ),
        Exercise(
            name = "Stretching gatto-mucca",
            category = ExerciseCategory.MOBILITY,
            muscleGroup = "Colonna, mobilità",
            description = "In quadrupedia, alterna inarcamento e incurvamento della schiena al ritmo del respiro.",
            isTimeBased = true, defaultSets = 2, defaultDurationSec = 60, defaultRestSec = 20,
            imageAsset = "exercises/cat_cow.jpg",
        ),
    )
}
