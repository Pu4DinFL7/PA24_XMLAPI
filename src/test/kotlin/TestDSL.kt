import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestDSL {

    @Test
    fun testDocumentCreationWithDSL() {
        val doc = xmlDocument({
            root("ReinosNórdicos"){
                child("Midgard") {
                    addAttribute("Região", "Lago Dos Nove")
                    addAttribute("Povo", "Humanos e mortais")
                    addAttribute("Sítio", "Conselho das Valquírias")
                }
                child("Alfheim"){
                    addAttribute("Região", "Lago da Luz")
                    addAttribute("Povo", "Elfos")
                    addAttribute("Curiosidade", "Brok não pode entrar devido a roubo e mau paleio")
                }
                child("Asgard"){
                    addAttribute("Povo", "Deuses Aesir")
                    child("Odin"){
                        addAttribute("Deus", "Aesir")
                        addAttribute("Característica1", "Cruel")
                        addAttribute("Característica2", "Tirano")
                        addAttribute("Característica3", "Astuto")
                        addAttribute("Característica4", "Gosta de Corvos")
                        addAttribute("Característica5", "Não gosta de Kratos")
                    }
                }
                child("Vanaheim"){
                    addAttribute("Povo", "Deuses Vanir")
                    addAttribute("Curiosidade", "Freya é uma tipa porreira")
                }
                child("Niflheim"){
                    addAttribute("aka", "Gelo")
                    addAttribute("Maluco", "Ivaldi o anão alquimista")
                }
                child("Muspelheim"){
                    addAttribute("aka", "Fogo")
                    addAttribute("Deus", "Surtr")
                }
                child("Helheim"){
                    addAttribute("aka", "purgatório")
                    addAttribute("Povo", "Mortos em desonra + Hel, Deusa da morte")
                }
                child("Svartalfheim"){
                    addAttribute("Povo", "Ananicos")
                }
                child("Jötunheim"){
                    addAttribute("Povo", "Jötnar")
                    addAttribute("Curiosidade", "Mimir sabe do portal que leva a Jötunheim")
                }
            }
        },{
            assertEquals("ReinosNórdicos\n" +
                    "├── Midgard\n" +
                    "├── Alfheim\n" +
                    "├── Asgard\n" +
                    "│   └── Odin\n" +
                    "├── Vanaheim\n" +
                    "├── Niflheim\n" +
                    "├── Muspelheim\n" +
                    "├── Helheim\n" +
                    "├── Svartalfheim\n" +
                    "└── Jötunheim", toTree())
            //toXML("Yggdrasil")
        })

        assertThrows<IllegalArgumentException> {
            xmlDocument({
                root("root1")
                root("root2")
            })}

        assertEquals(doc.getEntities(){ent -> ent.name =="Midgard"}, doc["Midgard"])

        val list = mutableListOf<LinkedHashMap<String,String>>()
        doc.getEntities().forEach(){e-> list.add(e.getAttributes() { key, _ -> key == "Curiosidade" })}
        assertEquals(list, doc.getEntities()["Curiosidade"])

        val a = xmlEntity("Regular-Show"){
            child("Mordecai"){
                addAttribute("Mordequices", "1")
            }
            child("Rigby")
            addAttribute("Eggcelent","1")
        }

        assertEquals(linkedMapOf("Mordequices" to "1"),(a / "Mordecai")["Mordequices"])

    }
}