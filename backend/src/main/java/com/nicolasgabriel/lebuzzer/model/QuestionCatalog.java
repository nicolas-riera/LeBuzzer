package com.nicolasgabriel.lebuzzer.model;

import java.util.Arrays;
import java.util.List;

public class QuestionCatalog {

    // Science & Technology
    public static final Question Q_THERMAL_CONDUCTIVITY = new Question(
        1,
        "Which chemical element has the highest thermal conductivity of any known bulk material?",
        Arrays.asList("Gold", "Diamond", "Graphene", "Copper"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_HTTPS_PORT = new Question(
        2,
        "What is the standard port number used for HTTPS traffic?",
        Arrays.asList("80", "21", "443", "8080"),
        Arrays.asList(2),
        20
    );

    public static final Question Q_JAVA_CREATOR = new Question(
        3,
        "Which programming language was created by James Gosling at Sun Microsystems?",
        Arrays.asList("Python", "Java", "C++", "C#"),
        Arrays.asList(1),
        25
    );

    public static final Question Q_PRIME_NUMBERS = new Question(
        4,
        "Which of the following are considered prime numbers?",
        Arrays.asList("21", "29", "31", "33"),
        Arrays.asList(1, 2),
        25
    );

    public static final Question Q_NON_VOLATILE_MEMORY = new Question(
        5,
        "What type of memory in a computer is both non-volatile and reprogrammable?",
        Arrays.asList("SRAM", "DRAM", "Flash Memory", "Cache"),
        Arrays.asList(2),
        15
    );

    public static final Question Q_ELECTRON_CHARGE = new Question(
        6,
        "Which of these subatomic particles carry a negative electric charge?",
        Arrays.asList("Proton", "Electron", "Neutron", "Up Quark"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_KIDNEY_NEPHRON = new Question(
        7,
        "What is the functional unit of the human kidney responsible for filtering blood?",
        Arrays.asList("Neuron", "Nephron", "Alveolus", "Hepatomere"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_TITAN_LAKES = new Question(
        8,
        "Which planetary body in the Solar System is known to have liquid hydrocarbon lakes on its surface?",
        Arrays.asList("Europa", "Titan", "Ganymede", "Enceladus"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_SPEED_OF_LIGHT = new Question(
        9,
        "What is the approximate speed of light in a vacuum?",
        Arrays.asList("150,000 km/s", "300,000 km/s", "1,000,000 km/s", "30,000 km/s"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_DNA_STRUCTURE = new Question(
        10,
        "What shape best describes the structure of a DNA molecule?",
        Arrays.asList("Single Helix", "Double Helix", "Triple Helix", "Tetrahedron"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_NOBEL_GASES = new Question(
        11,
        "Which of the following elements are classified as noble gases?",
        Arrays.asList("Helium", "Neon", "Chlorine", "Argon"),
        Arrays.asList(0, 1, 3),
        25
    );

    public static final Question Q_LINUX_KERNEL = new Question(
        12,
        "Who created the Linux operating system kernel in 1991?",
        Arrays.asList("Richard Stallman", "Linus Torvalds", "Ken Thompson", "Dennis Ritchie"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_TCP_HANDSHAKE = new Question(
        13,
        "Which steps are part of the standard TCP three-way handshake?",
        Arrays.asList("SYN", "SYN-ACK", "ACK", "FIN"),
        Arrays.asList(0, 1, 2),
        20
    );

    public static final Question Q_POWER_UNIT = new Question(
        14,
        "What is the SI unit of electrical power?",
        Arrays.asList("Joule", "Volt", "Watt", "Ampere"),
        Arrays.asList(2),
        15
    );

    public static final Question Q_AVOGADRO_NUMBER = new Question(
        15,
        "What does Avogadro's number measure?",
        Arrays.asList("Speed of sound", "Number of particles in a mole", "Atmospheric pressure", "Gravitational force"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_CPU_ARCHITECTURE = new Question(
        16,
        "Which CPU instruction set architecture is predominant in modern smartphones?",
        Arrays.asList("x86", "ARM", "RISC-V", "MIPS"),
        Arrays.asList(1),
        15
    );

    // History & World Events
    public static final Question Q_APOLLO_11 = new Question(
        17,
        "In which year did the Apollo 11 mission successfully land humans on the Moon?",
        Arrays.asList("1965", "1969", "1971", "1973"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_MACHU_PICCHU = new Question(
        18,
        "Which ancient civilization constructed the city of Machu Picchu in Peru?",
        Arrays.asList("Maya", "Aztec", "Inca", "Olmec"),
        Arrays.asList(2),
        15
    );

    public static final Question Q_ALLIED_FORCES_WW2 = new Question(
        19,
        "Which of these countries were official members of the Allied Forces during World War II?",
        Arrays.asList("United Kingdom", "United States", "Soviet Union", "Italy"),
        Arrays.asList(0, 1, 2),
        25
    );

    public static final Question Q_FIRST_EMPEROR_CHINA = new Question(
        20,
        "Who was the first emperor of unified China, responsible for starting the Great Wall construction?",
        Arrays.asList("Qin Shi Huang", "Han Wudi", "Kublai Khan", "Tang Taizong"),
        Arrays.asList(0),
        20
    );

    public static final Question Q_CONSTANTINOPLE_FALL = new Question(
        21,
        "The Fall of Constantinople in 1453 marked the end of which ancient empire?",
        Arrays.asList("Roman Empire", "Byzantine Empire", "Ottoman Empire", "Holy Roman Empire"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_MAGNA_CARTA = new Question(
        22,
        "In which year was the Magna Carta signed in England?",
        Arrays.asList("1066", "1215", "1492", "1776"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_FRENCH_REVOLUTION = new Question(
        23,
        "Which event in 1789 is widely considered the start of the French Revolution?",
        Arrays.asList("Execution of Louis XVI", "Storming of the Bastille", "Reign of Terror", "Battle of Waterloo"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_ROSETTA_STONE = new Question(
        24,
        "The Rosetta Stone was key to deciphering which ancient writing system?",
        Arrays.asList("Cuneiform", "Egyptian Hieroglyphs", "Linear B", "Sanskrit"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_BERLIN_WALL = new Question(
        25,
        "In which year did the Berlin Wall fall, leading to German reunification?",
        Arrays.asList("1985", "1989", "1991", "1993"),
        Arrays.asList(1),
        15
    );

    // Geography & Earth
    public static final Question Q_SPAIN_OCEANS = new Question(
        26,
        "Which country is bordered by both the Atlantic Ocean and the Mediterranean Sea?",
        Arrays.asList("Portugal", "Spain", "Italy", "Greece"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_MARIANA_TRENCH = new Question(
        27,
        "What is the deepest known ocean trench on Earth?",
        Arrays.asList("Puerto Rico Trench", "Java Trench", "Mariana Trench", "Tonga Trench"),
        Arrays.asList(2),
        15
    );

    public static final Question Q_CAPITAL_CITIES = new Question(
        28,
        "Which of these cities are official capitals of their respective sovereign nations?",
        Arrays.asList("Sydney", "Canberra", "Toronto", "Ottawa"),
        Arrays.asList(1, 3),
        20
    );

    public static final Question Q_URAL_MOUNTAINS = new Question(
        29,
        "Which mountain range serves as a natural border between Europe and Asia?",
        Arrays.asList("Alps", "Ural Mountains", "Andes", "Pyrenees"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_LANDLOCKED_KAZAKHSTAN = new Question(
        30,
        "What is the largest landlocked country in the world by surface area?",
        Arrays.asList("Mongolia", "Kazakhstan", "Bolivia", "Chad"),
        Arrays.asList(1),
        20
    );

    public static final Question Q_LONGEST_RIVER = new Question(
        31,
        "Which river is widely recognized as the longest river in the world?",
        Arrays.asList("Amazon River", "Nile River", "Yangtze River", "Mississippi River"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_HIGHEST_ACTIVE_VOLCANO = new Question(
        32,
        "Where is the highest active volcano on Earth, Ojos del Salado, located?",
        Arrays.asList("Japan", "Iceland", "Chile and Argentina border", "Indonesia"),
        Arrays.asList(2),
        20
    );

    public static final Question Q_NORDIC_COUNTRIES = new Question(
        33,
        "Which of the following are sovereign Nordic countries?",
        Arrays.asList("Norway", "Finland", "Iceland", "Estonia"),
        Arrays.asList(0, 1, 2),
        25
    );

    public static final Question Q_LARGEST_DESERT = new Question(
        34,
        "What is the largest desert in the world by total surface area?",
        Arrays.asList("Sahara Desert", "Gobi Desert", "Antarctic Desert", "Arabian Desert"),
        Arrays.asList(2),
        20
    );

    // Literature, Arts & Pop Culture
    public static final Question Q_MOBY_DICK = new Question(
        35,
        "Who wrote the epic 19th-century novel 'Moby-Dick'?",
        Arrays.asList("Charles Dickens", "Herman Melville", "Mark Twain", "Nathaniel Hawthorne"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_PARASITE_OSCAR = new Question(
        36,
        "Which movie won the Academy Award for Best Picture in the year 2020?",
        Arrays.asList("1917", "Parasite", "Once Upon a Time in Hollywood", "Joker"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_DA_VINCI_PAINTINGS = new Question(
        37,
        "Which painting was created by the Italian Renaissance artist Leonardo da Vinci?",
        Arrays.asList("The Starry Night", "Mona Lisa", "The Last Supper", "The Persistence of Memory"),
        Arrays.asList(1, 2),
        20
    );

    public static final Question Q_MOUNT_DOOM = new Question(
        38,
        "In 'The Lord of the Rings', what is the name of the volcano where the One Ring was forged?",
        Arrays.asList("Mount Doom", "Lonely Mountain", "Erebor", "Mount Mindolluin"),
        Arrays.asList(0),
        15
    );

    public static final Question Q_SHAKESPEARE_HAMLET = new Question(
        39,
        "In Shakespeare's 'Hamlet', what is the famous opening line of Hamlet's soliloquy?",
        Arrays.asList("To be, or not to be", "Shall I compare thee to a summer's day?", "Now is the winter of our discontent", "Friends, Romans, countrymen"),
        Arrays.asList(0),
        15
    );

    public static final Question Q_IMPRESSIONISM = new Question(
        40,
        "Which of these painters were prominent figures in the Impressionist movement?",
        Arrays.asList("Claude Monet", "Edgar Degas", "Pablo Picasso", "Pierre-Auguste Renoir"),
        Arrays.asList(0, 1, 3),
        25
    );

    public static final Question Q_HARRY_POTTER_HOUSES = new Question(
        41,
        "Which house in Hogwarts is represented by a raven as its mascot?",
        Arrays.asList("Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin"),
        Arrays.asList(2),
        15
    );

    public static final Question Q_BEETHOVEN_SYMPHONY = new Question(
        42,
        "Which Beethoven symphony features the famous 'Ode to Joy' choral finale?",
        Arrays.asList("Symphony No. 3", "Symphony No. 5", "Symphony No. 7", "Symphony No. 9"),
        Arrays.asList(3),
        15
    );

    // General Knowledge & Gaming
    public static final Question Q_JAPAN_CURRENCY = new Question(
        43,
        "What is the main currency used in Japan?",
        Arrays.asList("Won", "Yuan", "Yen", "Baht"),
        Arrays.asList(2),
        10
    );

    public static final Question Q_UNREAL_ENGINE = new Question(
        44,
        "Which game engine was developed by Epic Games?",
        Arrays.asList("Unity", "Unreal Engine", "Godot", "CryEngine"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_CHESS_KNIGHT = new Question(
        45,
        "In chess, which piece can move in an 'L' shape and leap over other pieces?",
        Arrays.asList("Bishop", "Rook", "Knight", "Queen"),
        Arrays.asList(2),
        10
    );

    public static final Question Q_MINECRAFT_DEVELOPER = new Question(
        46,
        "Which video game development company original created Minecraft?",
        Arrays.asList("Mojang", "Valve", "Blizzard", "Ubisoft"),
        Arrays.asList(0),
        10
    );

    public static final Question Q_OLYMPIC_RINGS = new Question(
        47,
        "How many interlocking rings are featured on the official Olympic flag?",
        Arrays.asList("4", "5", "6", "7"),
        Arrays.asList(1),
        10
    );

    public static final Question Q_PLANET_COUNT = new Question(
        48,
        "How many recognized planets are currently in our Solar System?",
        Arrays.asList("7", "8", "9", "10"),
        Arrays.asList(1),
        10
    );

    public static final Question Q_MARIO_CREATOR = new Question(
        49,
        "Who is the iconic Japanese game designer behind Super Mario and The Legend of Zelda?",
        Arrays.asList("Hideo Kojima", "Shigeru Miyamoto", "Eiji Aonuma", "Hidetaka Miyazaki"),
        Arrays.asList(1),
        15
    );

    public static final Question Q_FASTEST_LAND_ANIMAL = new Question(
        50,
        "What is the fastest land animal over short distances?",
        Arrays.asList("Cheetah", "Pronghorn", "Lion", "Gazelle"),
        Arrays.asList(0),
        10
    );

    public static List<Question> getAllQuestions() {
        return List.of(
            Q_THERMAL_CONDUCTIVITY, Q_HTTPS_PORT, Q_JAVA_CREATOR, Q_PRIME_NUMBERS,
            Q_NON_VOLATILE_MEMORY, Q_ELECTRON_CHARGE, Q_KIDNEY_NEPHRON, Q_TITAN_LAKES,
            Q_SPEED_OF_LIGHT, Q_DNA_STRUCTURE, Q_NOBEL_GASES, Q_LINUX_KERNEL,
            Q_TCP_HANDSHAKE, Q_POWER_UNIT, Q_AVOGADRO_NUMBER, Q_CPU_ARCHITECTURE,
            Q_APOLLO_11, Q_MACHU_PICCHU, Q_ALLIED_FORCES_WW2, Q_FIRST_EMPEROR_CHINA,
            Q_CONSTANTINOPLE_FALL, Q_MAGNA_CARTA, Q_FRENCH_REVOLUTION, Q_ROSETTA_STONE,
            Q_BERLIN_WALL, Q_SPAIN_OCEANS, Q_MARIANA_TRENCH, Q_CAPITAL_CITIES,
            Q_URAL_MOUNTAINS, Q_LANDLOCKED_KAZAKHSTAN, Q_LONGEST_RIVER, Q_HIGHEST_ACTIVE_VOLCANO,
            Q_NORDIC_COUNTRIES, Q_LARGEST_DESERT, Q_MOBY_DICK, Q_PARASITE_OSCAR,
            Q_DA_VINCI_PAINTINGS, Q_MOUNT_DOOM, Q_SHAKESPEARE_HAMLET, Q_IMPRESSIONISM,
            Q_HARRY_POTTER_HOUSES, Q_BEETHOVEN_SYMPHONY, Q_JAPAN_CURRENCY, Q_UNREAL_ENGINE,
            Q_CHESS_KNIGHT, Q_MINECRAFT_DEVELOPER, Q_OLYMPIC_RINGS, Q_PLANET_COUNT,
            Q_MARIO_CREATOR, Q_FASTEST_LAND_ANIMAL
        );
    }
}