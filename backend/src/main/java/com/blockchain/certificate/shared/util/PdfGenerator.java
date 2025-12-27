package com.blockchain.certificate.shared.util;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * PDF 生成工具类
 * 使用 iText7 库生成证书 PDF
 */
@Component
public class PdfGenerator {

    private static final String DEFAULT_FONT = "STSong-Light";
    private static final String DEFAULT_ENCODING = "UniGB-UCS2-H";

    /**
     * 生成证书 PDF
     * @param templateData 模板数据
     * @param fieldValues 字段值映射
     * @return PDF 字节数组
     */
    public byte[] generateCertificatePdf(CertificateTemplate templateData, Map<String, String> fieldValues) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // 设置中文字体
            PdfFont font = createChineseFont();
            
            // 添加背景图片（如果有）
            if (templateData.getBackgroundImagePath() != null) {
                addBackgroundImage(document, templateData.getBackgroundImagePath());
            }

            // 添加标题
            addTitle(document, font, fieldValues.getOrDefault("title", "证书"));

            // 添加证书内容
            addCertificateContent(document, font, fieldValues);

            // 添加签名信息
            addSignatureInfo(document, font, fieldValues);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成 PDF 失败", e);
        }
    }

    /**
     * 创建中文字体
     */
    private PdfFont createChineseFont() {
        try {
            // 尝试使用系统字体
            return PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
        } catch (Exception e) {
            try {
                // 如果系统字体不可用，使用内置字体
                return PdfFontFactory.createFont();
            } catch (IOException ex) {
                throw new RuntimeException("创建字体失败", ex);
            }
        }
    }

    /**
     * 添加背景图片
     */
    private void addBackgroundImage(Document document, String imagePath) {
        try {
            ImageData imageData = ImageDataFactory.create(imagePath);
            Image backgroundImage = new Image(imageData);
            
            // 设置图片大小和位置
            backgroundImage.setFixedPosition(0, 0);
            backgroundImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            
            document.add(backgroundImage);
        } catch (Exception e) {
            // 如果背景图片加载失败，继续生成 PDF
            System.err.println("背景图片加载失败: " + e.getMessage());
        }
    }

    /**
     * 添加标题
     */
    private void addTitle(Document document, PdfFont font, String title) {
        Paragraph titleParagraph = new Paragraph()
                .add(new Text(title).setFont(font).setFontSize(24))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(100)
                .setMarginBottom(50);
        
        document.add(titleParagraph);
    }

    /**
     * 添加证书内容
     */
    private void addCertificateContent(Document document, PdfFont font, Map<String, String> fieldValues) {
        // 证书编号
        String certificateNo = fieldValues.getOrDefault("certificateNo", "");
        if (!certificateNo.isEmpty()) {
            Paragraph certNoParagraph = new Paragraph()
                    .add(new Text("证书编号：" + certificateNo).setFont(font).setFontSize(12))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(certNoParagraph);
        }

        // 持有人姓名
        String holderName = fieldValues.getOrDefault("holderName", "");
        if (!holderName.isEmpty()) {
            Paragraph holderParagraph = new Paragraph()
                    .add(new Text("兹证明 ").setFont(font).setFontSize(16))
                    .add(new Text(holderName).setFont(font).setFontSize(18).setBold())
                    .add(new Text(" 同学").setFont(font).setFontSize(16))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(holderParagraph);
        }

        // 证书内容
        String content = fieldValues.getOrDefault("content", "");
        if (!content.isEmpty()) {
            Paragraph contentParagraph = new Paragraph()
                    .add(new Text(content).setFont(font).setFontSize(14))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(40);
            document.add(contentParagraph);
        }

        // 学院信息
        String college = fieldValues.getOrDefault("college", "");
        String major = fieldValues.getOrDefault("major", "");
        if (!college.isEmpty() || !major.isEmpty()) {
            String collegeInfo = "";
            if (!college.isEmpty()) {
                collegeInfo += college;
            }
            if (!major.isEmpty()) {
                collegeInfo += " " + major + "专业";
            }
            
            Paragraph collegeParagraph = new Paragraph()
                    .add(new Text(collegeInfo).setFont(font).setFontSize(14))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(collegeParagraph);
        }

        // 颁发日期
        String issueDate = fieldValues.getOrDefault("issueDate", "");
        if (!issueDate.isEmpty()) {
            Paragraph dateParagraph = new Paragraph()
                    .add(new Text("颁发日期：" + issueDate).setFont(font).setFontSize(12))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(50);
            document.add(dateParagraph);
        }
    }

    /**
     * 添加签名信息
     */
    private void addSignatureInfo(Document document, PdfFont font, Map<String, String> fieldValues) {
        // 颁发机构
        String issuer = fieldValues.getOrDefault("issuer", "");
        if (!issuer.isEmpty()) {
            Paragraph issuerParagraph = new Paragraph()
                    .add(new Text(issuer).setFont(font).setFontSize(14))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginRight(100)
                    .setMarginBottom(20);
            document.add(issuerParagraph);
        }

        // 签名日期
        String signDate = fieldValues.getOrDefault("signDate", "");
        if (!signDate.isEmpty()) {
            Paragraph signDateParagraph = new Paragraph()
                    .add(new Text(signDate).setFont(font).setFontSize(12))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginRight(100);
            document.add(signDateParagraph);
        }
    }

    /**
     * 生成简单的证书 PDF（用于测试）
     * @param certificateNo 证书编号
     * @param holderName 持有人姓名
     * @param title 证书标题
     * @param content 证书内容
     * @return PDF 字节数组
     */
    public byte[] generateSimpleCertificatePdf(String certificateNo, String holderName, 
                                             String title, String content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // 设置字体
            PdfFont font = createChineseFont();

            // 标题
            Paragraph titleParagraph = new Paragraph()
                    .add(new Text(title).setFont(font).setFontSize(24).setBold())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(100)
                    .setMarginBottom(50);
            document.add(titleParagraph);

            // 证书编号
            Paragraph certNoParagraph = new Paragraph()
                    .add(new Text("证书编号：" + certificateNo).setFont(font).setFontSize(12))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(certNoParagraph);

            // 持有人
            Paragraph holderParagraph = new Paragraph()
                    .add(new Text("兹证明 ").setFont(font).setFontSize(16))
                    .add(new Text(holderName).setFont(font).setFontSize(18).setBold())
                    .add(new Text(" 同学").setFont(font).setFontSize(16))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(40);
            document.add(holderParagraph);

            // 内容
            Paragraph contentParagraph = new Paragraph()
                    .add(new Text(content).setFont(font).setFontSize(14))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(80);
            document.add(contentParagraph);

            // 签名
            Paragraph signatureParagraph = new Paragraph()
                    .add(new Text("颁发机构").setFont(font).setFontSize(14))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginRight(100);
            document.add(signatureParagraph);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成简单 PDF 失败", e);
        }
    }

    /**
     * 证书模板数据类
     */
    public static class CertificateTemplate {
        private String backgroundImagePath;
        private Map<String, FieldConfig> fields;

        public CertificateTemplate() {}

        public CertificateTemplate(String backgroundImagePath, Map<String, FieldConfig> fields) {
            this.backgroundImagePath = backgroundImagePath;
            this.fields = fields;
        }

        public String getBackgroundImagePath() {
            return backgroundImagePath;
        }

        public void setBackgroundImagePath(String backgroundImagePath) {
            this.backgroundImagePath = backgroundImagePath;
        }

        public Map<String, FieldConfig> getFields() {
            return fields;
        }

        public void setFields(Map<String, FieldConfig> fields) {
            this.fields = fields;
        }
    }

    /**
     * 字段配置类
     */
    public static class FieldConfig {
        private float x;
        private float y;
        private int fontSize;
        private String alignment;

        public FieldConfig() {}

        public FieldConfig(float x, float y, int fontSize, String alignment) {
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
            this.alignment = alignment;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public String getAlignment() {
            return alignment;
        }

        public void setAlignment(String alignment) {
            this.alignment = alignment;
        }
    }
}