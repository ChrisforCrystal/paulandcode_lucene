package com.paulandcode.utils;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 附件上传工具类
 * @Author: paulandcode
 * @Email: paulandcode@gmail.com
 * @Date: 2018/9/23 17:09
 */
public class FileUtils {
    /**
     * Excel类型
     */
    private static final String SUFFIX_2003 = ".xls";
    private static final String SUFFIX_2007 = ".xlsx";
    /**
     * 科学记数法关键字
     */
    private static final String E = "E";

    /**
     * 数字格式化, 用于将科学记数法转换成非科学记数法
     */
    public NumberFormat nf;

    /**
     * 日期格式
     */
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 读取Excel内容
     *
     * @param file            上传的附件
     * @param reesultCellType 转换后的结果类型, 必须为String或者Object
     * @return java.util.List<java.util.List   <   java.util.List   <   java.lang.Object>>>
     */
    public static <T> List<List<List<T>>> excelToList(MultipartFile file, Class<T> reesultCellType) {
        boolean toString;
        if (reesultCellType == String.class) {
            toString = true;
        } else if (reesultCellType == Object.class) {
            toString = false;
        } else {
            throw new RuntimeException("reesultCellType只支持String和Object! ");
        }
        List<List<List<T>>> result = new ArrayList<>();
        if (file == null) {
            throw new RuntimeException("文件不存在! ");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件格式错误! ");
        }
        Workbook workbook = null;
        FormulaEvaluator formulaEvaluator = null;
        try {
            if (originalFilename.endsWith(SUFFIX_2003)) {
                workbook = new HSSFWorkbook(file.getInputStream());
                formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
            } else if (originalFilename.endsWith(SUFFIX_2007)) {
                workbook = new XSSFWorkbook(file.getInputStream());
                formulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件格式错误! ");
        }
        if (workbook == null) {
            throw new RuntimeException("文件格式错误! ");
        } else {
            int latSheetNum = workbook.getNumberOfSheets();
            // 遍历每一个Sheet页
            for (int i = 0; i < latSheetNum; i++) {
                List<List<T>> rows = new ArrayList<>();
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    int lastRowNum = sheet.getLastRowNum();
                    // 遍历每一行, 行这里有等号
                    for (int j = 0; j <= lastRowNum; j++) {
                        List<T> cells = new ArrayList<>();
                        Row row = sheet.getRow(j);
                        if (row != null) {
                            int lastCellNum = row.getLastCellNum();
                            // 遍历每一列
                            for (int k = 0; k < lastCellNum; k++) {
                                Cell cell = row.getCell(k);
                                if (cell != null) {
                                    // noinspection unchecked
                                    cells.add((T) getCellValue(cell, formulaEvaluator, toString));
                                }
                            }
                        }
                        rows.add(cells);
                    }
                }
                result.add(rows);
            }
        }
        return result;
    }

    /**
     * 获得单元格中的值
     *
     * @param cell             单元格
     * @param formulaEvaluator 公式解析器
     * @param toString         是否将结果都转换成字符串
     * @return java.lang.Object
     */
    private static Object getCellValue(Cell cell, FormulaEvaluator formulaEvaluator, boolean toString) {
        Object content;
        CellType type = cell.getCellTypeEnum();
        if (type == CellType.BLANK) {
            content = "";
        } else if (type == CellType.NUMERIC) {
            // 首先判断是否为日期类型, 因为日期也是NUMERIC类型
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                content = cell.getDateCellValue();
                if (toString) {
                    content = DEFAULT_DATE_FORMAT.format(content);
                }
            } else {
                content = cell.getNumericCellValue();
                if (toString) {
                    content = removeE(content);
                }
            }
        } else if (type == CellType.STRING) {
            content = cell.getStringCellValue();
        } else if (type == CellType.BOOLEAN) {
            content = cell.getBooleanCellValue();
            if (toString) {
                content = String.valueOf(content);
            }
        } else if (type == CellType.FORMULA) {
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            CellType cellValueType = cellValue.getCellTypeEnum();
            // CellValue 的类型只有数字, 字符串, 布尔三种. 若都不是, 则返回原公式.
            if (cellValueType == CellType.NUMERIC) {
                content = cellValue.getNumberValue();
                if (toString) {
                    content = removeE(content);
                }
            } else if (cellValueType == CellType.STRING) {
                content = cellValue.getStringValue();
            } else if (cellValueType == CellType.BOOLEAN) {
                content = cellValue.getBooleanValue();
                if (toString) {
                    content = String.valueOf(content);
                }
            } else {
                content = cell.getCellFormula();
            }
        } else {
            content = "Error: 该单元格无法解析! ";
        }
        return content;
    }

    /**
     * 去掉科学记数法并将.0结尾的字符串去掉.0
     *
     * @param content
     * @return java.lang.Object
     */
    public static Object removeE(Object content) {
        String stringContent = String.valueOf(content);
        if (stringContent.contains(E)) {
            NumberFormat nf = NumberFormat.getInstance();
            // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]
            nf.setGroupingUsed(false);
            // 保留小数后100位
            nf.setMaximumFractionDigits(100);
            stringContent = nf.format(content);
        }
        // 如果是整数, double会自动最后面加.0, 这里把.0去掉
        content = stringContent.replaceAll("\\.0$", "");
        return content;
    }

    /**
     * 删除整个文件夹
     *
     * @param dir 要删除的文件夹
     * @return void
     */
    public static void deleteAll(File dir) {
        if (dir == null) {
            System.out.println("不存在文件: " + dir);
        } else if (dir.isFile()) {
            System.out.println("删除文件: " + dir + " : " + dir.delete());
        } else {
            File[] files = dir.listFiles();
            for (File file : files) {
                deleteAll(file);
            }
            System.out.println("删除文件夹: " + dir + " : " + dir.delete());
        }
    }
}
