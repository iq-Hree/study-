package com.example.studyquest.data.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfSampleGenerator {

    fun generateMolecularBiologyPdf(context: Context): File {
        val file = File(context.filesDir, "Bioscience_Molecular_Biology.pdf")
        if (file.exists() && file.length() > 0) return file

        val document = PdfDocument()

        // Page 1: Introduction to Molecular Genetics & DNA Structure
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(60, 40, 110)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(100, 70, 160)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 11.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val accentBoxPaint = Paint().apply {
            color = Color.rgb(240, 235, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(180, 160, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        // Header band
        canvas1.drawRect(40f, 40f, 555f, 110f, accentBoxPaint)
        canvas1.drawRect(40f, 40f, 555f, 110f, borderPaint)
        canvas1.drawText("StudyQuest Bioscience Series: Volume 1", 55f, 65f, headerPaint)
        canvas1.drawText("Fundamentals of Molecular Biology & Genetics", 55f, 95f, titlePaint)

        // Section 1
        var y = 140f
        canvas1.drawText("1. Structure of Deoxyribonucleic Acid (DNA)", 50f, y, headerPaint)
        y += 24f
        val p1Lines = listOf(
            "DNA is a polymer composed of two polynucleotide chains that coil around each other to form a",
            "double helix. The polymer comprises four nitrogenous bases: Adenine (A), Thymine (T),",
            "Cytosine (C), and Guanine (G). Erwin Chargaff's rules dictate that the quantity of Adenine equals",
            "Thymine (A = T with two hydrogen bonds) and Guanine equals Cytosine (G = C with three hydrogen bonds).",
            "",
            "The antiparallel nature of DNA means one strand runs 5' to 3' while the complementary strand",
            "runs 3' to 5'. The major and minor grooves facilitate sequence-specific binding of regulatory proteins",
            "such as transcription factors and polymerases."
        )
        for (line in p1Lines) {
            canvas1.drawText(line, 50f, y, bodyPaint)
            y += 18f
        }

        y += 15f
        canvas1.drawText("2. DNA Replication Machinery", 50f, y, headerPaint)
        y += 24f
        val p2Lines = listOf(
            "Replication is semiconservative: each daughter molecule contains one parental and one newly",
            "synthesized strand. Key enzymes coordinating this complex molecular choreography include:",
            "",
            "• Helicase: Unwinds the double-stranded DNA at the replication fork.",
            "• Single-Strand Binding Proteins (SSBs): Stabilize single strands to prevent re-annealing.",
            "• Topoisomerase (Gyrase): Relieves supercoiling and torsional strain ahead of the fork.",
            "• Primase: Synthesizes a short RNA primer to provide a free 3'-OH group for elongation.",
            "• DNA Polymerase III: Catalyzes the addition of deoxynucleotides in the 5' -> 3' direction.",
            "• DNA Polymerase I: Removes RNA primers via 5' to 3' exonuclease activity and replaces them with DNA.",
            "• DNA Ligase: Seals nicked phosphodiester backbones on the lagging strand (Okazaki fragments)."
        )
        for (line in p2Lines) {
            canvas1.drawText(line, 50f, y, bodyPaint)
            y += 18f
        }

        // Summary box at bottom
        y += 25f
        canvas1.drawRect(50f, y, 545f, y + 80f, accentBoxPaint)
        canvas1.drawRect(50f, y, 545f, y + 80f, borderPaint)
        canvas1.drawText("Key Examination Takeaway:", 65f, y + 25f, headerPaint)
        canvas1.drawText("Lagging strand synthesis occurs discontinuously due to the unidirectional activity of DNA", 65f, y + 45f, bodyPaint)
        canvas1.drawText("polymerase, generating Okazaki fragments that are joined by DNA ligase.", 65f, y + 65f, bodyPaint)

        canvas1.drawText("Page 1 of 3", 270f, 810f, bodyPaint)
        document.finishPage(page1)

        // Page 2: Transcription and RNA Processing
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        val canvas2 = page2.canvas

        y = 60f
        canvas2.drawText("3. Transcription: From DNA to Messenger RNA", 50f, y, headerPaint)
        y += 24f
        val p3Lines = listOf(
            "Transcription converts genetic information encoded in DNA into functional RNA transcripts.",
            "It is divided into three distinct phases: Initiation, Elongation, and Termination.",
            "",
            "A. Initiation: RNA Polymerase II recognizes promoter sequences (e.g., the TATA box in eukaryotes).",
            "Transcription factor complexes (TFIID, TFIIB, TFIIH) assemble the pre-initiation complex.",
            "",
            "B. Elongation: RNA Polymerase synthesizes RNA in the 5' to 3' direction without requiring a primer.",
            "The DNA duplex rewinds immediately downstream of the transcription bubble.",
            "",
            "C. Termination: Cleavage and polyadenylation specificity factors recognize poly-A signals (AAUAAA)."
        )
        for (line in p3Lines) {
            canvas2.drawText(line, 50f, y, bodyPaint)
            y += 18f
        }

        y += 20f
        canvas2.drawText("4. Eukaryotic Post-Transcriptional Modifications", 50f, y, headerPaint)
        y += 24f
        val p4Lines = listOf(
            "Primary transcripts (pre-mRNA) undergo three crucial processing events before nucleocytoplasmic export:",
            "1. 5' 7-Methylguanosine Capping: Protects RNA from 5'-exonuclease degradation and aids ribosome binding.",
            "2. 3' Polyadenylation: Addition of 150-250 Adenine residues enhancing transcript stability and translation.",
            "3. Splicing: Spliceosomes (composed of snRNAs and proteins) excise introns and ligate exons.",
            "   Alternative splicing creates protein diversity from single genes, a hallmark of organismal complexity."
        )
        for (line in p4Lines) {
            canvas2.drawText(line, 50f, y, bodyPaint)
            y += 18f
        }

        y += 30f
        canvas2.drawRect(50f, y, 545f, y + 90f, accentBoxPaint)
        canvas2.drawRect(50f, y, 545f, y + 90f, borderPaint)
        canvas2.drawText("Clinical Relevance & Bioscience Skills Note:", 65f, y + 25f, headerPaint)
        canvas2.drawText("Mutations in splice sites cause severe diseases including beta-thalassemia and cystic fibrosis.", 65f, y + 45f, bodyPaint)
        canvas2.drawText("Targeted antisense oligonucleotides (ASOs) can correct aberrant splicing therapeutically.", 65f, y + 65f, bodyPaint)

        canvas2.drawText("Page 2 of 3", 270f, 810f, bodyPaint)
        document.finishPage(page2)

        // Page 3: Translation and the Genetic Code
        val pageInfo3 = PdfDocument.PageInfo.Builder(595, 842, 3).create()
        val page3 = document.startPage(pageInfo3)
        val canvas3 = page3.canvas

        y = 60f
        canvas3.drawText("5. Translation: Ribosomes and Polypeptide Synthesis", 50f, y, headerPaint)
        y += 24f
        val p5Lines = listOf(
            "The genetic code is degenerate (redundant), universal, and non-overlapping. Sixty-one codons code",
            "for 20 amino acids, with AUG serving as the initiation codon (Methionine) and UAA, UAG, and UGA",
            "acting as termination stop signals.",
            "",
            "Ribosomal Architecture:",
            "• Eukaryotic 80S ribosome (composed of 40S small and 60S large subunits).",
            "• A (Aminoacyl) site: Receives incoming aminoacyl-tRNA matching the codon.",
            "• P (Peptidyl) site: Holds the tRNA linked to the growing nascent polypeptide chain.",
            "• E (Exit) site: Discharges uncharged tRNA back into the cytoplasm for recharging by aminoacyl-tRNA synthetase.",
            "",
            "Peptidyl transferase activity is a ribozyme function catalyzed by the 28S rRNA of the large subunit,",
            "highlighting the ancient RNA World evolutionary hypothesis."
        )
        for (line in p5Lines) {
            canvas3.drawText(line, 50f, y, bodyPaint)
            y += 18f
        }

        y += 25f
        canvas3.drawText("6. Study Review Questions", 50f, y, headerPaint)
        y += 24f
        val reviewQuestions = listOf(
            "Q1. Why does DNA polymerase require an RNA primer while RNA polymerase does not?",
            "Q2. Contrast the biological implications of transition vs transversion point mutations.",
            "Q3. How does the wobble hypothesis explain tRNA redundancy during translation?",
            "Q4. Outline the steps involved in PCR (Polymerase Chain Reaction) and its thermocycling phases."
        )
        for (q in reviewQuestions) {
            canvas3.drawText(q, 50f, y, bodyPaint)
            y += 20f
        }

        canvas3.drawText("Page 3 of 3", 270f, 810f, bodyPaint)
        document.finishPage(page3)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun generatePhysiologyPrimerPdf(context: Context): File {
        val file = File(context.filesDir, "Human_Physiology_Essentials.pdf")
        if (file.exists() && file.length() > 0) return file

        val document = PdfDocument()
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(40, 80, 110)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(50, 110, 140)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 11.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val accentBoxPaint = Paint().apply {
            color = Color.rgb(235, 245, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(160, 200, 230)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        canvas1.drawRect(40f, 40f, 555f, 110f, accentBoxPaint)
        canvas1.drawRect(40f, 40f, 555f, 110f, borderPaint)
        canvas1.drawText("StudyQuest Bioscience Series: Volume 2", 55f, 65f, headerPaint)
        canvas1.drawText("Human Physiology: Neurobiology & Homeostasis", 55f, 95f, titlePaint)

        var y = 140f
        canvas1.drawText("1. Cellular Homeostasis and Resting Membrane Potential", 50f, y, headerPaint)
        y += 24f
        val lines = listOf(
            "Homeostasis is the physiological maintenance of a stable internal environment despite external perturbations.",
            "Excitable cells (neurons, cardiomyocytes) sustain an electrical potential across their plasma membrane,",
            "typically around -70 mV. This resting potential is maintained principally by:",
            "",
            "• Na+/K+-ATPase Pump: Expels 3 Na+ ions for every 2 K+ ions imported, expending 1 ATP.",
            "• Potassium Leak Channels: High resting permeability to K+ establishes the equilibrium potential (Nernst eq).",
            "• Impermeant Intracellular Anions: Proteins and organic phosphates trapped in the cytosol."
        )
        for (l in lines) {
            canvas1.drawText(l, 50f, y, bodyPaint)
            y += 18f
        }

        y += 20f
        canvas1.drawText("2. Action Potential Phases and Propagation", 50f, y, headerPaint)
        y += 24f
        val apLines = listOf(
            "1. Depolarization: Stimulus crosses threshold (-55 mV), triggering rapid opening of voltage-gated Na+ channels.",
            "2. Peak Overshoot: Membrane potential surges to +30 mV; Na+ channels inactivate via the ball-and-chain mechanism.",
            "3. Repolarization: Voltage-gated K+ channels open, allowing rapid efflux of potassium ions.",
            "4. Hyperpolarization: Delayed closure of K+ channels creates an undershoot (-80 mV).",
            "5. Refractory Periods: Absolute refractory period prevents retro-propagation; relative period requires larger stimulus.",
            "6. Saltatory Conduction: Myelinated axons propagate potentials rapidly from node of Ranvier to node of Ranvier."
        )
        for (l in apLines) {
            canvas1.drawText(l, 50f, y, bodyPaint)
            y += 18f
        }

        canvas1.drawText("Page 1 of 2", 270f, 810f, bodyPaint)
        document.finishPage(page1)

        // Page 2
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        val canvas2 = page2.canvas

        y = 60f
        canvas2.drawText("3. Synaptic Transmission & Neurotransmitters", 50f, y, headerPaint)
        y += 24f
        val synLines = listOf(
            "When the action potential invades the presynaptic terminal, voltage-gated Ca2+ channels open.",
            "Intracellular calcium influx triggers SNARE proteins (synaptobrevin, syntaxin, SNAP-25) to mediate",
            "vesicle docking and exocytosis of neurotransmitter into the synaptic cleft.",
            "",
            "• Acetylcholine (ACh): Mediates neuromuscular junctions; broken down by acetylcholinesterase (AChE).",
            "• Glutamate: Primary excitatory neurotransmitter in the CNS, acting on NMDA and AMPA ionotropic receptors.",
            "• GABA: Primary inhibitory neurotransmitter, opening Cl- channels to hyperpolarize the postsynaptic membrane.",
            "• Dopamine & Serotonin: Monoamines governing reward pathways, motor control, mood, and sleep cycles."
        )
        for (l in synLines) {
            canvas2.drawText(l, 50f, y, bodyPaint)
            y += 18f
        }

        y += 25f
        canvas2.drawRect(50f, y, 545f, y + 80f, accentBoxPaint)
        canvas2.drawRect(50f, y, 545f, y + 80f, borderPaint)
        canvas2.drawText("Bioscience Laboratory Observation:", 65f, y + 25f, headerPaint)
        canvas2.drawText("Tetrodotoxin (TTX) from pufferfish selectively blocks voltage-gated sodium channels, preventing", 65f, y + 45f, bodyPaint)
        canvas2.drawText("action potential firing and causing progressive flaccid paralysis.", 65f, y + 65f, bodyPaint)

        canvas2.drawText("Page 2 of 2", 270f, 810f, bodyPaint)
        document.finishPage(page2)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }
}
