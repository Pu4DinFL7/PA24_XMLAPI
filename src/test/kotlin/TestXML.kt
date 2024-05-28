import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestXML {

    @Test
    fun testEntityFunctionalities(){
        /*
        *  Entity Creation
        */
        assertThrows<IllegalArgumentException> { XMLEntity("|") }
        val test = XMLEntity("toTest")
        test.addAttribute("notSpecialName",  "Spe<ial\"\" Nam&")

        val irithyll = XMLEntity("irithyll")
        val sulyvahnsbeast = XMLEntity("sullyvahnsbeast", irithyll)
        val pontiffsullyvahn = XMLEntity("pontiffsullyvahn", irithyll)
        assertEquals(sulyvahnsbeast.parent, irithyll)
        assertEquals(irithyll.children, listOf(sulyvahnsbeast) + listOf(pontiffsullyvahn))
        assertEquals(listOf<XMLEntity>(), pontiffsullyvahn.children)

        /*
        *  Attribute Manipulation
        */
        // Attribute Addition
        irithyll.addAttribute("boss", "Pontiff Sullyvahn")
        irithyll.addAttribute("enemy", "Sewer Centipede")
        assertEquals(linkedMapOf("boss" to "Pontiff Sullyvahn", "enemy" to "Sewer Centipede"), irithyll.getAttributes())

        sulyvahnsbeast.addAllAttributes(linkedMapOf("eyes" to "6", "anatomy" to "canine", "respawn" to "false"))
        assertEquals(linkedMapOf("eyes" to "6", "anatomy" to "canine", "respawn" to "false"), sulyvahnsbeast.getAttributes())

        pontiffsullyvahn.addAttribute("eyes", "2")
        pontiffsullyvahn.addAllAttributes(linkedMapOf("anatomy" to "humanoid", "respawn" to "false", "swords" to "1"))
        assertEquals(linkedMapOf("eyes" to "2", "anatomy" to "humanoid", "respawn" to "false", "swords" to "1"), pontiffsullyvahn.getAttributes())
        assertThrows<IllegalArgumentException> {         pontiffsullyvahn.addAttribute("swords", "2") }
        assertEquals(linkedMapOf("eyes" to "2", "anatomy" to "humanoid", "respawn" to "false", "swords" to "1"), pontiffsullyvahn.getAttributes())


        // Attribute Removal
        irithyll.removeAttribute("enemy")
        assertEquals(linkedMapOf("boss" to "Pontiff Sullyvahn"), irithyll.getAttributes())
        sulyvahnsbeast.addAllAttributes(linkedMapOf("flying" to "yes", "fishing" to "maybe", "sitting" to "sometimes be like"))
        sulyvahnsbeast.removeAllAttributes(){ s1, _ -> s1.contains("ing")}
        assertEquals(linkedMapOf("eyes" to "6", "anatomy" to "canine", "respawn" to "false"), sulyvahnsbeast.getAttributes())

        //Attribute Edition
        pontiffsullyvahn.editAttribute("swords", "2")
        pontiffsullyvahn.addAllAttributes(linkedMapOf("friendly" to "yes", "cute" to "yes", "non-frustrating" to "absolutely"))
        pontiffsullyvahn.editAttribute("year", "2011")
        pontiffsullyvahn.editAllAttributes({ s1, s2 -> s1 == "friendly" || s2 == "absolutely" || s1 == "cute"}, "AHA! You wish!")
        assertEquals(linkedMapOf("eyes" to "2", "anatomy" to "humanoid", "respawn" to "false", "swords" to "2", "friendly" to "AHA! You wish!", "cute" to "AHA! You wish!", "non-frustrating" to "AHA! You wish!"), pontiffsullyvahn.getAttributes())
    }

    @Test
    fun testDocumentFunctionalities(){

        /*
         *  Document Creation
         */
        val ffx = XMLEntity("Final_Fantasy_X")
        val characters = XMLEntity("Characters", ffx)

        val tidus = XMLEntity("Tidus", characters, linkedMapOf("Age" to "21"))
        val auron = XMLEntity("Auron", characters, linkedMapOf("Age" to "44"))
        val yuna = XMLEntity("Yuna", characters, linkedMapOf("Age" to "19"))

        val locations = XMLEntity("Locations", ffx)

        val calm_lands = XMLEntity("Calm_lands", locations, linkedMapOf("Curiosity" to "first battle was here"))
        val zanarkand = XMLEntity("Zanarkand", locations, linkedMapOf("Curiosity" to "best music"))
        val doc = XMLDocument(ffx)

        //println(doc.toXML("doc"))
        assertThrows<IllegalArgumentException> { doc.version = "123456" }
        assertThrows<IllegalArgumentException> { doc.encoding = "abcd" }

        /*
        Document Manipulation
         */

        // add entity "Omega_ruins" and "Rikku"
        doc.addEntity("Omega_ruins"){e -> e.contains("Locations")}
        assertEquals("[Calm_lands Curiosity=\"first battle was here\", Zanarkand Curiosity=\"best music\", Omega_ruins]",doc.getEntities({e -> e.parent== locations}).toString() )
        doc.addEntity("Rikku"){e -> e.contains("Characters")}
        assertEquals("[Tidus Age=\"21\", Auron Age=\"44\", Yuna Age=\"19\", Rikku]", doc.getEntities({e -> e.parent== characters}).toString())

        /*
        *  Attribute Manipulation
        */
        // Add atribute  "release_date" to the rootEntity, and attribute "weapon" to all the characters

        doc.addAttribute("release_date", "2001"){ entityName -> entityName.startsWith("F")}
        assertEquals(linkedMapOf("release_date" to "2001"), ffx.getAttributes())
        assertThrows<IllegalArgumentException> {doc.addAttribute("release_date", "2013"){ entityName -> entityName.startsWith("F")}}

        doc.addAttribute("weapon", "caladbolg"){e -> e.contains("Tidus")}
        doc.addAttribute("weapon", "nirvana"){e -> e.contains("Yuna")}
        doc.addAttribute("weapon", "masamune"){e -> e.contains("Auron")}

        assertEquals(linkedMapOf("Age" to "21","weapon" to "caladbolg"), tidus.getAttributes())



        // remove entity Rikky and Omega, previously created
        doc.removeEntity { e-> e.contains("Rikku") }
        assertEquals("[Tidus Age=\"21\" weapon=\"caladbolg\", Auron Age=\"44\" weapon=\"masamune\", Yuna Age=\"19\" weapon=\"nirvana\"]",doc.getEntities({e -> e.parent == characters}).toString())

        doc.removeEntity { e -> e.contains("Omega") }
        assertEquals("[Calm_lands Curiosity=\"first battle was here\", Zanarkand Curiosity=\"best music\"]", doc.getEntities({e -> e.parent == locations}).toString())


        // remove attribute "Age" to all entities
        doc.removeAttribute("Age"){true}
        assertEquals(linkedMapOf("weapon" to "masamune"), auron.getAttributes())
        assertEquals(linkedMapOf("weapon" to "caladbolg"), tidus.getAttributes())
        assertEquals(linkedMapOf("weapon" to "nirvana"), yuna.getAttributes())



        // edit attribute "curiosity" to "Wonderfull game" in Zanarkand entity

        zanarkand.editAttribute("Curiosity", "very nostalgic")
        assertEquals(linkedMapOf("Curiosity" to "very nostalgic"), zanarkand.getAttributes())

        calm_lands.addAttribute("People", "Kind people in this region")
        assertEquals(linkedMapOf("Curiosity" to "first battle was here", "People" to "Kind people in this region"), calm_lands.getAttributes())

        doc.editAttribute("Curiosity", "Wonderfull game", {e -> e == zanarkand.fullName})
        assertEquals(linkedMapOf("Curiosity" to "Wonderfull game"), zanarkand.getAttributes())



        // rename entity "Calm lands" to "Besaid Island"

        doc.renameEntity("Besaid_Island"){e -> e== calm_lands.fullName}
        assertEquals("Besaid_Island", calm_lands.name)

        doc.toXML("doc")
    }

    @Test
    fun testToXML(){
        val plano = XMLEntity("plano")
        val doc = XMLDocument(plano)
        val curso = XMLEntity("curso", plano)
        curso.plainText = "Mestrado em Engenharia Informática"
        val fuc1 = XMLEntity("fuc", plano)
        fuc1.addAttribute("codigo", "M4310")
        val nome = XMLEntity("nome", fuc1)
        nome.plainText="Programação Avançada"
        val ects = XMLEntity("ects", fuc1)
        ects.plainText = "6.0"
        val avaliação = XMLEntity("avaliacao", fuc1)
        val comp1 = XMLEntity("componente", avaliação)
        comp1.addAllAttributes(linkedMapOf("nome" to "Quizzes", "peso" to "20%"))
        val comp2 = XMLEntity("componente", avaliação)
        comp2.addAllAttributes(linkedMapOf("nome" to "Projeto", "peso" to "80%"))

        val fuc2 = XMLEntity("fuc", plano)
        fuc2.addAttribute("codigo", "03782")
        val nome2 = XMLEntity("nome", fuc2)
        nome2.plainText="Dissertação"
        val ects2 = XMLEntity("ects", fuc2)
        ects2.plainText = "42.0"
        val avaliação2 = XMLEntity("avaliacao", fuc2)
        val comp12 = XMLEntity("componente", avaliação2)
        comp12.addAllAttributes(linkedMapOf("nome" to "Dissertação", "peso" to "60%"))
        val comp22 = XMLEntity("componente", avaliação2)
        comp22.addAllAttributes(linkedMapOf("nome" to "Apresentação", "peso" to "20%"))
        val comp32 = XMLEntity("componente", avaliação2)
        comp32.addAllAttributes(linkedMapOf("nome" to "Discussão", "peso" to "20%"))

        doc.toXML("exEnunciado")
        assertEquals(15, doc.getEntities { true }.size)

    }

    @Test
    fun testXPath(){
        val plano = XMLEntity("plano")
        val doc = XMLDocument(plano)
        val curso = XMLEntity("curso", plano)
        curso.plainText = "Mestrado em Engenharia Informática"
        val fuc1 = XMLEntity("fuc", plano)
        fuc1.addAttribute("codigo", "M4310")
        val nome = XMLEntity("nome", fuc1)
        nome.plainText="Programação Avançada"
        val ects = XMLEntity("ects", fuc1)
        ects.plainText = "6.0"
        val avaliação = XMLEntity("avaliacao", fuc1)
        val comp1 = XMLEntity("componente", avaliação)
        comp1.addAllAttributes(linkedMapOf("nome" to "Quizzes", "peso" to "20%"))
        val comp2 = XMLEntity("componente", avaliação)
        comp2.addAllAttributes(linkedMapOf("nome" to "Projeto", "peso" to "80%"))

        val fuc2 = XMLEntity("fuc", plano)
        fuc2.addAttribute("codigo", "03782")
        val nome2 = XMLEntity("nome", fuc2)
        nome2.plainText="Dissertação"
        val ects2 = XMLEntity("ects", fuc2)
        ects2.plainText = "42.0"
        val avaliação2 = XMLEntity("avaliacao", fuc2)
        val comp12 = XMLEntity("componente", avaliação2)
        comp12.addAllAttributes(linkedMapOf("nome" to "Dissertação", "peso" to "60%"))
        val comp22 = XMLEntity("componente", avaliação2)
        comp22.addAllAttributes(linkedMapOf("nome" to "Apresentação", "peso" to "20%"))
        val comp32 = XMLEntity("componente", avaliação2)
        comp32.addAllAttributes(linkedMapOf("nome" to "Discussão", "peso" to "20%"))

        assertEquals(listOf("<componente nome=\"Quizzes\" peso=\"20%\"/>","<componente nome=\"Projeto\" peso=\"80%\"/>","<componente nome=\"Dissertação\" peso=\"60%\"/>","<componente nome=\"Apresentação\" peso=\"20%\"/>","<componente nome=\"Discussão\" peso=\"20%\"/>"), doc.queryXPath("fuc/avaliacao/componente"))
    }
}
