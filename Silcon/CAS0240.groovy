import br.com.multitec.utils.collections.TableMap
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
//import com.itextpdf.text.*
//import com.itextpdf.text.pdf.*
import javax.swing.JButton
import java.awt.Desktop;
//import java.io.File;


public class Script extends sam.swing.ScriptBase {

    MultitecRootPanel panel;
    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.panel = tarefa;
//        criarBotaoGerarPDF();
        reordenarColunas();
    }
    private void reordenarColunas(){
        MSpread sprItens = getComponente("sprItens");
        sprItens.getColumnIndex("abm01codigo") != -1 ? sprItens.moveColumn(sprItens.getColumnIndex("abm01codigo"), 0) : null;
        sprItens.getColumnIndex("abm01descr") != -1 ? sprItens.moveColumn(sprItens.getColumnIndex("abm01descr"), 1) : null;
        sprItens.getColumnIndex("preco") != -1 ? sprItens.moveColumn(sprItens.getColumnIndex("preco"), 2) : null;
    }

    /*
    private void gerarPDF() {

        try {

            MSpread sprItens = getComponente("sprItens");
            if(sprItens.getValue().size() == 0) return;

            String caminho = System.getProperty("user.home") + "/Downloads/relatorio_itens.pdf"//"C:/Users/Leonardo/Desktop/TESTE/relatorio.pdf"

            // Documento com margens
            Document document = new Document(PageSize.A4, 20, 20, 20, 20)
            PdfWriter.getInstance(document, new FileOutputStream(caminho))
            document.open()

            // Fontes
            Font fonteTitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
            Font fonteNormal = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL)
            Font fonteHeader = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)

            // Título
            Paragraph titulo = new Paragraph("Relatório Itens", fonteTitulo)
            titulo.setAlignment(Element.ALIGN_CENTER)
            document.add(titulo)

            document.add(new Paragraph(" "))

            // =========================
            // TABELA
            // =========================
            PdfPTable tabela = new PdfPTable(3)

            // Usa toda largura disponível respeitando margem
            tabela.setWidthPercentage(100)

            // Proporção das colunas
            tabela.setWidths([20, 60, 20] as float[])

            // ===== Cabeçalho =====
            PdfPCell c1 = new PdfPCell(new Phrase("CÓDIGO", fonteHeader))
            PdfPCell c2 = new PdfPCell(new Phrase("DESCRIÇÃO", fonteHeader))
            PdfPCell c3 = new PdfPCell(new Phrase("PREÇO", fonteHeader))

            // Alinhamento
            c1.setHorizontalAlignment(Element.ALIGN_LEFT)
            c2.setHorizontalAlignment(Element.ALIGN_LEFT)
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT)

            // Remove bordas padrão
            [c1, c2, c3].each { cell ->
                cell.setBorder(Rectangle.NO_BORDER)
                cell.setPaddingBottom(5)
            }

            // Linha separadora (só embaixo do header)
            [c1, c2, c3].each { cell ->
                cell.setBorderWidthBottom(1)
            }

            tabela.addCell(c1)
            tabela.addCell(c2)
            tabela.addCell(c3)

            def idsItens = new ArrayList();
            // ===== Dados =====
            for(item in sprItens.getValue()){
                idsItens.add(item.getLong("id"));
            }

            def dados = buscarListaItens(idsItens)

            dados.each { linha ->

                PdfPCell d1 = new PdfPCell(new Phrase(linha[0], fonteNormal))
                PdfPCell d2 = new PdfPCell(new Phrase(linha[1], fonteNormal))
                PdfPCell d3 = new PdfPCell(new Phrase(linha[2], fonteNormal))

                d1.setHorizontalAlignment(Element.ALIGN_LEFT)
                d2.setHorizontalAlignment(Element.ALIGN_LEFT)
                d3.setHorizontalAlignment(Element.ALIGN_RIGHT)

                // Sem bordas nas linhas
                [d1, d2, d3].each { cell ->
                    cell.setBorder(Rectangle.NO_BORDER)
                    cell.setPaddingBottom(3)
                }

                tabela.addCell(d1)
                tabela.addCell(d2)
                tabela.addCell(d3)
            }

            // Adiciona tabela no PDF
            document.add(tabela)

            document.close();

            abrirPastaArquivo(new File(caminho))

            //exibirInformacao("PDF criado com sucesso!")


        } catch (Exception e) {
            e.printStackTrace()
        }
    }

     */
    private buscarListaItens(def idsItens){
        String sql = "SELECT abm01codigo as codigo, abm01descr AS descr, abe4001preco AS preco " +
                "FROM abe4001 " +
                "INNER JOIN abm01 ON abm01id = abe4001item " +
                "WHERE abm01id IN " + idsItens.toString().replace("[","(").replace("]", ")") +
                "AND abe4001tab = 92848 " +
                "ORDER BY abm01descr";

        def itens = executarConsulta(sql);
        def itensTab = new ArrayList();

        for(item in itens){
            def listItens = new ArrayList();
            String codItem = item.getString("codigo");
            String descr = item.getString("descr");
            String preco = item.getBigDecimal_Zero("preco").round(2).toString();

            listItens.add(codItem);
            listItens.add(descr);
            listItens.add(preco);

            itensTab.add(listItens);
        }

        return itensTab;
    }

    private void criarBotaoGerarPDF(){
        JButton btnGerarPDF = new JButton();
        btnGerarPDF.setText("Salvar PDF");
        btnGerarPDF.setBounds(613, 0, 126, 20);

        btnGerarPDF.addActionListener(e -> btnGerarPDFPressed())

        panel.add(btnGerarPDF)
    }
    private void btnGerarPDFPressed(){
        gerarPDF();
    }
    private static void abrirPastaArquivo(File pdfFile) {
        try {
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}