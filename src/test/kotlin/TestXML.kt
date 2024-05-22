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
        pontiffsullyvahn.addAttribute("swords", "2")
        assertEquals(linkedMapOf("eyes" to "2", "anatomy" to "humanoid", "respawn" to "false", "swords" to "2"), pontiffsullyvahn.getAttributes())


        // Attribute Removal
        irithyll.removeAttribute("enemy")
        assertEquals(linkedMapOf("boss" to "Pontiff Sullyvahn"), irithyll.getAttributes())
        sulyvahnsbeast.addAllAttributes(linkedMapOf("flying" to "yes", "fishing" to "maybe", "sitting" to "sometimes be like"))
        sulyvahnsbeast.removeAllAttributes(){ s1, _ -> s1.contains("ing")}
        assertEquals(linkedMapOf("eyes" to "6", "anatomy" to "canine", "respawn" to "false"), sulyvahnsbeast.getAttributes())

        //Attribute Edition
        pontiffsullyvahn.addAllAttributes(linkedMapOf("friendly" to "yes", "cute" to "yes", "non-frustrating" to "absolutely"))
        pontiffsullyvahn.editAttribute("year", "2011")
        pontiffsullyvahn.editAllAttributes({ s1, s2 -> s1 == "friendly" || s2 == "absolutely" || s1 == "cute"}, "AHA! You wish!")
        assertEquals(linkedMapOf("eyes" to "2", "anatomy" to "humanoid", "respawn" to "false", "swords" to "2", "friendly" to "AHA! You wish!", "cute" to "AHA! You wish!", "non-frustrating" to "AHA! You wish!"), pontiffsullyvahn.getAttributes())

    }
//
//    @Test
//    fun testDocumentFunctionalities(){
//        val a = XMLEntity("a")
//        val b = XMLEntity("b", a)
//        XMLEntity("b", a,hashMapOf("anoFabricação" to "1938"))
//        val b3 = XMLEntity("b", a,hashMapOf("Banshee" to "44"))
//        XMLEntity("c", a)
//        val d = XMLEntity("d", b)
//        XMLEntity("e", d)
//        val doc = XMLDocument(a)
//
//        // adicionar um atributo à entidade
//        doc.addAttribute("Cayde", "06"){ entityName -> entityName.startsWith("b")}
//        assertEquals(hashMapOf("Banshee" to "44","Cayde" to "06" ),b3.getAttributes())
//
//        //remover um atributo à entidade
//        doc.removeAttribute("Cayde") { entityName -> entityName.startsWith("b") }
//        assertEquals(hashMapOf("Banshee" to "44"), b3.getAttributes())
//
//        doc.editAttribute("Banshee", "98"){ entityName -> entityName == b3.fullName }
//
//        assertEquals(hashMapOf("Banshee" to "98"), b3.getAttributes())
//
//        doc.addEntity("f"){ entityName -> entityName == "e"}
//        doc.addEntity("g"){ entityName -> entityName == "e"}
//        assertEquals("a\n" +
//                "|-b\n" +
//                "||-d\n" +
//                "|||-e\n" +
//                "||||-f\n" +
//                "||||-g\n" +
//                "|-b\n" +
//                "|-b\n" +
//                "|-c", doc.toTree())
//
//
//        doc.removeEntity { entityName -> entityName == "g" }
//
//        assertEquals("a\n" +
//                "|-b\n" +
//                "||-d\n" +
//                "|||-e\n" +
//                "||||-f\n" +
//                "|-b\n" +
//                "|-b\n" +
//                "|-c", doc.toTree())
//
//        doc.renameEntity("ola"){entityName -> entityName == "f"}
//
//        assertEquals("a\n" +
//                "|-b\n" +
//                "||-d\n" +
//                "|||-e\n" +
//                "||||-ola\n" +
//                "|-b\n" +
//                "|-b\n" +
//                "|-c", doc.toTree())
//
//
//    }
//
//    @Test
//    fun testToXML(){
//        val plano = XMLEntity("plano")
//        val doc = XMLDocument(plano)
//        val curso = XMLEntity("curso", plano)
//        curso.plainText = "Mestrado em Engenharia Informática"
//        val fuc1 = XMLEntity("fuc", plano)
//        fuc1.addAttribute("codigo", "M4310")
//        val nome = XMLEntity("nome", fuc1)
//        nome.plainText="Programação Avançada"
//        val ects = XMLEntity("ects", fuc1)
//        ects.plainText = "6.0"
//        val avaliação = XMLEntity("avaliacao", fuc1)
//        val comp1 = XMLEntity("componente", avaliação)
//        comp1.addAllAttributes(hashMapOf("nome" to "Quizzes", "peso" to "20%"))
//        val comp2 = XMLEntity("componente", avaliação)
//        comp2.addAllAttributes(hashMapOf("nome" to "Projeto", "peso" to "80%"))
//
//        val fuc2 = XMLEntity("fuc", plano)
//        fuc2.addAttribute("codigo", "03782")
//        val nome2 = XMLEntity("nome", fuc2)
//        nome2.plainText="Dissertação"
//        val ects2 = XMLEntity("ects", fuc2)
//        ects2.plainText = "42.0"
//        val avaliação2 = XMLEntity("avaliacao", fuc2)
//        val comp12 = XMLEntity("componente", avaliação2)
//        comp12.addAllAttributes(hashMapOf("nome" to "Dissertação", "peso" to "60%"))
//        val comp22 = XMLEntity("componente", avaliação2)
//        comp22.addAllAttributes(hashMapOf("nome" to "Apresentação", "peso" to "20%"))
//        val comp32 = XMLEntity("componente", avaliação2)
//        comp32.addAllAttributes(hashMapOf("nome" to "Discussão", "peso" to "20%"))
//
//        doc.toXML("exEnunciado")
//        assertEquals(15, doc.getEntities { true }.size)
//
//    }
//
//    @Test
//    fun testXPath(){
//        val plano = XMLEntity("plano")
//        val doc = XMLDocument(plano)
//        val curso = XMLEntity("curso", plano)
//        curso.plainText = "Mestrado em Engenharia Informática"
//        val fuc1 = XMLEntity("fuc", plano)
//        fuc1.addAttribute("codigo", "M4310")
//        val nome = XMLEntity("nome", fuc1)
//        nome.plainText="Programação Avançada"
//        val ects = XMLEntity("ects", fuc1)
//        ects.plainText = "6.0"
//        val avaliação = XMLEntity("avaliacao", fuc1)
//        val comp1 = XMLEntity("componente", avaliação)
//        comp1.addAllAttributes(hashMapOf("nome" to "Quizzes", "peso" to "20%"))
//        val comp2 = XMLEntity("componente", avaliação)
//        comp2.addAllAttributes(hashMapOf("nome" to "Projeto", "peso" to "80%"))
//
//        val fuc2 = XMLEntity("fuc", plano)
//        fuc2.addAttribute("codigo", "03782")
//        val nome2 = XMLEntity("nome", fuc2)
//        nome2.plainText="Dissertação"
//        val ects2 = XMLEntity("ects", fuc2)
//        ects2.plainText = "42.0"
//        val avaliação2 = XMLEntity("avaliacao", fuc2)
//        val comp12 = XMLEntity("componente", avaliação2)
//        comp12.addAllAttributes(hashMapOf("nome" to "Dissertação", "peso" to "60%"))
//        val comp22 = XMLEntity("componente", avaliação2)
//        comp22.addAllAttributes(hashMapOf("nome" to "Apresentação", "peso" to "20%"))
//        val comp32 = XMLEntity("componente", avaliação2)
//        comp32.addAllAttributes(hashMapOf("nome" to "Discussão", "peso" to "20%"))
//
//        assertEquals(listOf("<componente peso=\"20%\" nome=\"Quizzes\"/>", "<componente peso=\"80%\" nome=\"Projeto\"/>", "<componente peso=\"60%\" nome=\"Dissertação\"/>", "<componente peso=\"20%\" nome=\"Apresentação\"/>", "<componente peso=\"20%\" nome=\"Discussão\"/>"), doc.queryXPath("fuc"))
//
//    }
}
