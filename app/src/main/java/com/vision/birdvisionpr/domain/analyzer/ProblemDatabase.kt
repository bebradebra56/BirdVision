package com.vision.birdvisionpr.domain.analyzer

import com.vision.birdvisionpr.domain.model.ProblemEntry
import com.vision.birdvisionpr.domain.model.RiskLevel

object ProblemDatabase {

    val allProblems: List<ProblemEntry> = listOf(
        ProblemEntry(
            "Cannibalism",
            "A serious behavioral problem where birds peck at, injure, or eat each other. Often starts with feather pecking and escalates.",
            listOf("Overcrowding", "Boredom", "Nutritional imbalance (low protein/salt)", "Bright/uneven lighting", "Injuries triggering pecking instinct"),
            listOf("Immediately separate injured birds", "Reduce light intensity to 5–10 lux", "Increase space and enrichment", "Add 16–18% protein feed", "Consider beak trimming as last resort"),
            RiskLevel.CRITICAL
        ),
        ProblemEntry(
            "Chronic Stress",
            "Prolonged stress leads to immunosuppression, reduced production, and increased disease susceptibility.",
            listOf("Overcrowding", "Inconsistent routine", "Loud noises or predator threats", "Frequent flock changes", "Poor environment"),
            listOf("Establish consistent daily routines", "Reduce flock density", "Minimize sudden changes", "Provide enrichment and hiding spots"),
            RiskLevel.HIGH
        ),
        ProblemEntry(
            "Egg Production Drop",
            "A sudden or gradual decrease in the number of eggs laid per day.",
            listOf("Insufficient lighting (less than 14 hrs)", "Nutritional deficiency", "Molting cycle", "Disease or parasite", "Age of hens (over 2 years)", "Stress factors"),
            listOf("Maintain 14–16 hours of light daily", "Review calcium and protein intake", "Rule out disease with vet check", "Allow natural molt cycle annually"),
            RiskLevel.HIGH
        ),
        ProblemEntry(
            "Molting",
            "Natural annual process of feather replacement. Laying typically stops or decreases significantly during molt.",
            listOf("Seasonal daylight changes", "Nutritional stress", "Age — typically starts at 12–18 months"),
            listOf("Increase protein during molt (20%+)", "Do not force lighting during molt", "Provide calm, low-stress environment", "Expect 4–12 weeks before full recovery"),
            RiskLevel.MODERATE
        ),
        ProblemEntry(
            "Heat Stress",
            "Occurs when ambient temperature exceeds 27°C. Leads to panting, reduced activity, lower egg production, and death if severe.",
            listOf("High ambient temperature", "Poor ventilation", "High humidity", "Direct sun exposure"),
            listOf("Install tunnel ventilation or fans", "Provide shaded areas", "Refresh water multiple times daily", "Add electrolytes to water during heat waves"),
            RiskLevel.HIGH
        ),
        ProblemEntry(
            "Respiratory Disease",
            "Common in chickens, manifesting as sneezing, nasal discharge, wheezing, and reduced appetite.",
            listOf("Mycoplasma, Newcastle, or Infectious Bronchitis", "Dusty or poorly ventilated environment", "Cold, damp conditions", "Stress-induced immunosuppression"),
            listOf("Isolate symptomatic birds immediately", "Consult vet for diagnosis and vaccination", "Improve ventilation", "Disinfect housing thoroughly"),
            RiskLevel.CRITICAL
        ),
        ProblemEntry(
            "External Parasites",
            "Mites and lice cause irritation, feather loss, anemia, and can significantly reduce egg production.",
            listOf("Inadequate dust bathing area", "Poor litter management", "Wild bird contact", "Contaminated equipment"),
            listOf("Dust bath with diatomaceous earth", "Treat birds and coop with approved miticide", "Replace all bedding", "Inspect birds weekly"),
            RiskLevel.HIGH
        ),
        ProblemEntry(
            "Internal Parasites (Worms)",
            "Roundworms, cecal worms, and tapeworms reduce nutrient absorption and overall flock health.",
            listOf("Contaminated pasture", "Wild bird droppings", "Poor hygiene management"),
            listOf("Worm flock every 6 months", "Rotate pasture areas", "Maintain clean litter", "Consult vet for appropriate anthelmintic"),
            RiskLevel.MODERATE
        ),
        ProblemEntry(
            "Feed Deficiency",
            "Inadequate or imbalanced nutrition causes a cascade of health and production issues.",
            listOf("Low-quality feed", "Incorrect formulation for age/stage", "Stale or contaminated feed", "Insufficient calcium for layers"),
            listOf("Use age-appropriate layer pellets (16–18% protein)", "Provide oyster shell as calcium supplement", "Store feed in dry, sealed containers", "Replace feed every 2–4 weeks"),
            RiskLevel.MODERATE
        ),
        ProblemEntry(
            "Water Deprivation",
            "Even short periods without water dramatically reduce egg production and can be life-threatening.",
            listOf("Equipment failure", "Freezing in winter", "Insufficient drinker capacity", "Dirty or algae-filled water"),
            listOf("Check water systems daily", "Provide 1 drinker per 10 birds minimum", "Use heated waterers in winter", "Clean waterers weekly"),
            RiskLevel.CRITICAL
        ),
        ProblemEntry(
            "Egg Eating",
            "Hens learn to break and eat eggs — once established, extremely difficult to stop.",
            listOf("Accidental egg breakage", "Nutritional deficiency (calcium, protein)", "Boredom", "Overcrowding around nest boxes"),
            listOf("Collect eggs multiple times per day", "Ensure adequate calcium in diet", "Use roll-away nest boxes", "Reduce light in nest boxes"),
            RiskLevel.HIGH
        ),
        ProblemEntry(
            "Marek's Disease",
            "Highly contagious viral disease causing paralysis, tumors, and significant mortality in young birds.",
            listOf("Herpesvirus spread through feather dander", "Unvaccinated birds", "Introduction of carrier birds"),
            listOf("Vaccinate all chicks at hatch", "Quarantine new birds for 30 days", "Maintain strict biosecurity", "No cure — prevention only"),
            RiskLevel.CRITICAL
        ),
        ProblemEntry(
            "Sour Crop / Impacted Crop",
            "Feed accumulates in the crop and ferments or becomes blocked, causing illness.",
            listOf("Long grass ingestion", "Eating fibrous material", "Yeast overgrowth", "Lack of grit"),
            listOf("Provide grit in separate container", "Limit access to long grass", "Massage crop gently", "Consult vet for severe cases"),
            RiskLevel.MODERATE
        ),
        ProblemEntry(
            "Egg-Bound Hen",
            "A hen unable to pass an egg — a medical emergency requiring immediate attention.",
            listOf("Nutritional deficiency (calcium, Vitamin D)", "Young hens laying too early", "Large or misshapen eggs", "Stress or dehydration"),
            listOf("Provide warm bath soak (20 min)", "Apply lubrication gently", "Isolate and keep warm", "Consult vet immediately if not resolved"),
            RiskLevel.CRITICAL
        )
    )

    fun search(query: String): List<ProblemEntry> {
        if (query.isBlank()) return allProblems
        val q = query.lowercase()
        return allProblems.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.causes.any { c -> c.lowercase().contains(q) }
        }
    }
}
