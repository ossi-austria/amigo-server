package org.ossiaustria.amigo.platform.domain.services

import org.springframework.stereotype.Service
import java.util.Locale

@Service
class NameGeneratorService {
    companion object {
        val ADJECTIVES = listOf(
            "Bright", "Diligent", "Generous", "Happy", "Helpful",
            "Inventive", "Likable", "Loyal", "Reliable", "Sensible", "Sincere",
            "Witty", "Adorable", "Attractive", "Beautiful",
            "Clean", "Colorful", "Cute", "Elegant",
            "Fit", "Gorgeous", "Sleek", "Amazing",
            "Awesome", "Excellent", "Fabulous", "Fantastic",
            "Incredible", "Splendid", "Super",
            "Wonderful",
        )
        val ANIMALS = listOf(
            "Apes", "Badgers", "Bats", "Bears", "Bees", "Buffalo",
            "Camels", "Cats", "Kittens", "Cobras", "Crows", "Dogs",
            "Puppies", "Donkeys", "Eagles", "Elephants", "Elk", "Falcons",
            "Ferrets", "Fish", "Flamingos", "Foxes", "Frogs", "Geese",
            "Giraffes", "Gorillas", "Hyenas", "Jaguars", "Jellyfish",
            "Kangaroos", "Lemurs", "Leopards", "Lions", "Moles", "Monkeys",
            "Mules", "Otters", "Oxen", "Owls", "Parrots", "Rabbits",
            "Rats", "Ravens", "Shark", "Skunk", "Snakes",
            "Squirrels", "Stingrays", "Swans", "Tigers", "Toads", "Turkeys",
            "Turtles", "Weasels", "Whales", "Wolves", "Zebras"
        )
    }

    fun generateName(): String {
        val number = (10 + Math.random() * 89).toInt()
        val adjective = ADJECTIVES.shuffled().first()
        val noun = ANIMALS.shuffled().first()
        return "$number-$adjective-$noun".uppercase(Locale.getDefault())
    }
}
