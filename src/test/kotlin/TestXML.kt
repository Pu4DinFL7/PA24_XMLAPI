import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestXML {

    @Test
    fun testCreateEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val c = XMLEntity("c", a)
        assertEquals(b.parent, a)
        assertEquals(a.children, listOf(b) + listOf(c))
        val doc = XMLDocument(a)
        //assertEquals(doc.addEntity("e",d.name),e)
        //assertEquals(doc.removeEntity("e"),d)
    }
    @Test
    fun testVisitors(){
        val root = XMLEntity("root")
        val child1 = XMLEntity("children", root)
        val child2 = XMLEntity("doggo", root)
        val child3 = XMLEntity("Jamija", child2)
        root.addAllAttributes(hashMapOf("ola" to "123", "bling" to "blong"))
        child3.addAttribute("ChittyChitty", "Bang Bang")
        child1.entityPlainText = "I have lollipops!"

        val textXMLVisitor = XMLTextCollector()

        root.accept(textXMLVisitor)
        println(textXMLVisitor.collectedText)

        val doc = XMLDocument(root)
        doc.toXML("chitty")
    }

    @Test
    fun testAttributeVisitors(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val b2 = XMLEntity("b", a,hashMapOf("anoFabricação" to "1938"))
        val b3 = XMLEntity("b", a,hashMapOf("Banshee" to "44"))
        val c = XMLEntity("c", a)
        val d = XMLEntity("d", b)
        val e = XMLEntity("e", d)
        val doc = XMLDocument(a)

        // adicionar um atributo à entidade
        doc.addAttributeVisitor("Cayde", "06"){ entityName -> entityName.startsWith("b")}
        assertEquals(hashMapOf("Banshee" to "44","Cayde" to "06" ),b3.getAttributes())

        //remover um atributo à entidade
        doc.removeAttributeVisitor("Cayde") { entityName -> entityName.startsWith("b") }
        assertEquals(hashMapOf("Banshee" to "44"), b3.getAttributes())

        doc.editAttributeVisitor("Banshee", "98"){entityName -> entityName == b3.fullName }

        assertEquals(hashMapOf("Banshee" to "98"), b3.getAttributes())

        doc.addEntityVisitor("f"){entityName -> entityName == "e"}

        assertEquals("|a\n" +
                "\t|b\n" +
                "\t\t|d\n" +
                "\t\t\t|e\n" +
                "\t\t\t\t|f\n" +
                "\t|b\n" +
                "\t|b\n" +
                "\t|c", doc.toTree())



    }

    @Test
    fun testToXML(){
        val plano = XMLEntity("plano")
        val doc = XMLDocument(plano)
        val curso = XMLEntity("curso", plano)
        curso.entityPlainText = "Mestrado em Engenharia Informática"
        val fuc1 = XMLEntity("fuc", plano)
        fuc1.addAttribute("codigo", "M4310")
        val nome = XMLEntity("nome", fuc1)
        nome.entityPlainText="Programação Avançada"
        val ects = XMLEntity("ects", fuc1)
        ects.entityPlainText = "6.0"
        val avaliação = XMLEntity("avaliacao", fuc1)
        val comp1 = XMLEntity("componente", avaliação)
        comp1.addAllAttributes(hashMapOf("nome" to "Quizzes", "peso" to "20%"))
        val comp2 = XMLEntity("componente", avaliação)
        comp2.addAllAttributes(hashMapOf("nome" to "Projeto", "peso" to "80%"))

        val fuc2 = XMLEntity("fuc", plano)
        fuc2.addAttribute("codigo", "03782")
        val nome2 = XMLEntity("nome", fuc2)
        nome2.entityPlainText="Dissertação"
        val ects2 = XMLEntity("ects", fuc2)
        ects2.entityPlainText = "42.0"
        val avaliação2 = XMLEntity("avaliacao", fuc2)
        val comp12 = XMLEntity("componente", avaliação2)
        comp12.addAllAttributes(hashMapOf("nome" to "Dissertação", "peso" to "60%"))
        val comp22 = XMLEntity("componente", avaliação2)
        comp22.addAllAttributes(hashMapOf("nome" to "Apresentação", "peso" to "20%"))
        val comp32 = XMLEntity("componente", avaliação2)
        comp32.addAllAttributes(hashMapOf("nome" to "Discussão", "peso" to "20%"))

        doc.toXML("exEnunciado")
        assertEquals(15, doc.getEntities { true }.size)

    }

    @Test
    fun testXPath(){
        val plano = XMLEntity("plano")
        val doc = XMLDocument(plano)
        val curso = XMLEntity("curso", plano)
        curso.entityPlainText = "Mestrado em Engenharia Informática"
        val fuc1 = XMLEntity("fuc", plano)
        fuc1.addAttribute("codigo", "M4310")
        val nome = XMLEntity("nome", fuc1)
        nome.entityPlainText="Programação Avançada"
        val ects = XMLEntity("ects", fuc1)
        ects.entityPlainText = "6.0"
        val avaliação = XMLEntity("avaliacao", fuc1)
        val comp1 = XMLEntity("componente", avaliação)
        comp1.addAllAttributes(hashMapOf("nome" to "Quizzes", "peso" to "20%"))
        val comp2 = XMLEntity("componente", avaliação)
        comp2.addAllAttributes(hashMapOf("nome" to "Projeto", "peso" to "80%"))

        val fuc2 = XMLEntity("fuc", plano)
        fuc2.addAttribute("codigo", "03782")
        val nome2 = XMLEntity("nome", fuc2)
        nome2.entityPlainText="Dissertação"
        val ects2 = XMLEntity("ects", fuc2)
        ects2.entityPlainText = "42.0"
        val avaliação2 = XMLEntity("avaliacao", fuc2)
        val comp12 = XMLEntity("componente", avaliação2)
        comp12.addAllAttributes(hashMapOf("nome" to "Dissertação", "peso" to "60%"))
        val comp22 = XMLEntity("componente", avaliação2)
        comp22.addAllAttributes(hashMapOf("nome" to "Apresentação", "peso" to "20%"))
        val comp32 = XMLEntity("componente", avaliação2)
        comp32.addAllAttributes(hashMapOf("nome" to "Discussão", "peso" to "20%"))

        assertEquals(listOf("<componente peso=\"20%\" nome=\"Quizzes\"/>", "<componente peso=\"80%\" nome=\"Projeto\"/>", "<componente peso=\"60%\" nome=\"Dissertação\"/>", "<componente peso=\"20%\" nome=\"Apresentação\"/>", "<componente peso=\"20%\" nome=\"Discussão\"/>"), doc.queryXPath("fuc"))

    }
}
