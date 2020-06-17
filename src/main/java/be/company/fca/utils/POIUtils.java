package be.company.fca.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

public class POIUtils {

    /**
     * Crée un Workbook
     * @param xlsx indique si le format souhaité est xlsx (sinn, xls)
     * @return
     */
    public static Workbook createWorkbook(boolean xlsx){
        if (xlsx){
            return new XSSFWorkbook();
        } else {
            return new HSSFWorkbook();
        }
    }

    /**
     * Crée un Workbook à partir d'un fichier existant
     * @param inputStream stream issu du fichier existant
     * @throws IOException
     * @throws InvalidFormatException
     * @return
     */
    public static Workbook createWorkbook(InputStream inputStream) throws IOException, InvalidFormatException {
        return WorkbookFactory.create(inputStream);
    }

    /**
     * Recherche une sheet selon son nom
     * @param wb Workbook
     * @param sheetIndex index de la sheet (debute a 0)
     * @param create si true, créer le sheet si elle n'existe pas
     * @return
     */
    public static Sheet getSheet(Workbook wb, Integer sheetIndex, boolean create){
        if (sheetIndex != null) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            if (sheet == null && create){
                sheet = wb.createSheet();
            }
            return sheet;

        } else {
            return null;
        }
    }

    /**
     * Compte le nombre de colonnes d'une sheet
     * @param wb Workbool
     * @param sheetIndex index de la sheet (débute à 0)
     * @return Le nombre de colonne de la sheet
     */
    public static Integer countCols(Workbook wb, Integer sheetIndex) {
        Integer colsCounter = 0;
        if (sheetIndex != null) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            Row row = sheet.getRow(0);
            Iterator<Cell> cellHeaderIterator = row.cellIterator();
            while (cellHeaderIterator.hasNext()) {
                Cell cell = cellHeaderIterator.next();
                colsCounter++;
            }
        }else {
            return null;
        }
        return colsCounter;
    }

    /**
     * Lit la valeur d'une cellule
     * Attention, pour les dates :
     *  si la cellule excel est déclarée comme date, retourne un Long
     *  il convient mieux d'utiliser readDate
     *
     * @param sheet sheet sheet (0 based)
     * @param ri no de ligne no de ligne (0 based)
     * @param ci no de colonne no de colonne (0 based)
     * @return
     */
    public static Object read(Sheet sheet, Integer ri, Integer ci){
        Row row = sheet.getRow(ri);
        if (row == null){
            return null;
        } else {
            Cell cell = row.getCell(ci);
            if (cell == null){
                return null;
            } else {

                if (cell.getCellTypeEnum() == CellType.BOOLEAN){
                    return cell.getBooleanCellValue();

                } else if (cell.getCellTypeEnum() == CellType.STRING){
                    return cell.getStringCellValue();

                } else if (cell.getCellTypeEnum() == CellType.NUMERIC){
                    return cell.getNumericCellValue();

                }

                return null;

            }

        }

    }

    public static Boolean readAsBoolean(Sheet sheet, Integer ri, Integer ci){
        Row row = sheet.getRow(ri);
        if (row == null){
            return null;
        } else {
            Cell cell = row.getCell(ci);
            if (cell == null){
                return null;
            } else {
                if (cell.getCellTypeEnum() == CellType.BOOLEAN){
                    return cell.getBooleanCellValue();

                } else if (cell.getCellTypeEnum() == CellType.STRING){
                    String val = cell.getStringCellValue();
                    if (val != null){
                        val = val.toLowerCase().trim();
                    }
                    if ("1".equals(val) ||
                            "o".equals(val) || "y".equals(val) || "t".equals(val) || "v".equals(val) ||
                            "oui".equals(val) || "yes".equals(val) || "true".equals(val) || "vrai".equals(val)) {
                        return true;
                    } else {
                        return false;
                    }

                } else if (cell.getCellTypeEnum() == CellType.NUMERIC){
                    double val = cell.getNumericCellValue();
                    Integer i = Integer.valueOf((int) Math.floor(val));
                    if (i.equals(1)){
                        return true;
                    } else {
                        return false;
                    }
                }

                return null;
            }
        }

    }

    public static String readAsString(Sheet sheet, Integer ri, Integer ci){
        Row row = sheet.getRow(ri);
        if (row == null){
            return null;
        } else {
            Cell cell = row.getCell(ci);
            if (cell == null){
                return null;
            } else {
                if (cell.getCellTypeEnum() == CellType.BOOLEAN){
                    return Boolean.toString(cell.getBooleanCellValue());

                } else if (cell.getCellTypeEnum() == CellType.STRING){
                    return cell.getStringCellValue();

                } else if (cell.getCellTypeEnum() == CellType.NUMERIC){
                    double val = cell.getNumericCellValue();
                    if (Math.floor(val) < val){
                        return Double.toString(cell.getNumericCellValue());
                    } else {
                        //retirer le .0
                        Integer i = Integer.valueOf((int) Math.round(val));
                        return i.toString();
                    }
                }

                return null;
            }
        }

    }

    /**
     * Lit la valeur d'une cellule contenant une Date
     * @param sheet sheet
     * @param ri no de ligne
     * @param ci no de colonne
     * @return Date si la cellule excel est déclarée comme date, String s'il agit d'un String, sinon null
     */
    public static Object readDate(Sheet sheet, Integer ri, Integer ci){
        Row row = sheet.getRow(ri);
        if (row == null){
            return null;
        } else {
            Cell cell = row.getCell(ci);
            if (cell == null){
                return null;
            } else {

                if (cell.getCellTypeEnum() == CellType.STRING){
                    return cell.getStringCellValue();

                } else if (cell.getCellTypeEnum() == CellType.NUMERIC){
                    return cell.getDateCellValue();

                }

                return null;

            }

        }

    }

    /**
     * Permet de creer un style Excel pour les cellules de type Date
     * @param workbook
     * @return
     */
    public static CellStyle createDefaultDecimalCellStyle(Workbook workbook){
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        return cellStyle;
    }

    /**
     * Permet de creer un style Excel pour les cellules de type Date
     * @param workbook
     * @return
     */
    public static CellStyle createDefaultDateCellStyle(Workbook workbook){
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("d/m/yy"));
        return cellStyle;
    }

    /**
     * Ecrit une valeur dans une cellule
     * @param sheet sheet
     * @param ri ligne
     * @param ci colonne
     * @param value valeur
     *
     *  Privilegier la methode write avec l'argument dateCellStyle et decimalCellStyle
     */
    @Deprecated
    public static void write(Sheet sheet, Integer ri, Integer ci, Object value) {
        write(sheet,ri,ci,value,null,null);
    }

    /**
     * Ecrit une valeur dans une cellule
     * @param sheet sheet
     * @param ri ligne
     * @param ci colonne
     * @param value valeur
     * @param dateCellStyle Style pour la cellule de type Date
     */
    public static Cell write(Sheet sheet, Integer ri, Integer ci, Object value, CellStyle dateCellStyle, CellStyle decimalCellStyle){
        Row row = sheet.getRow(ri);
        if (row == null){
            row = sheet.createRow(ri);
        }

        Cell cell = row.getCell(ci);
        if (cell == null){
            cell = row.createCell(ci);

            if (value instanceof Date){
                cell.setCellType(CellType.NUMERIC);
                if (dateCellStyle==null){
                    CellStyle cellStyle = createDefaultDateCellStyle(sheet.getWorkbook());
                    cell.setCellStyle(cellStyle);
                }else{
                    cell.setCellStyle(dateCellStyle);
                }
            }

        }

        if (value instanceof Double){
            cell.setCellValue((Double) value);
            if (decimalCellStyle!=null){
                cell.setCellStyle(decimalCellStyle);
            }

        } else if (value instanceof Long){
            cell.setCellValue((Long) value);

        } else if (value instanceof Integer){
            cell.setCellValue((Integer) value);

        } else if (value instanceof Float){
            cell.setCellValue((Float) value);
            if (decimalCellStyle!=null){
                cell.setCellStyle(decimalCellStyle);
            }

        } else if (value != null && value instanceof BigDecimal){

            cell.setCellValue(((BigDecimal) value).doubleValue());
            if (decimalCellStyle!=null){
                cell.setCellStyle(decimalCellStyle);
            }

        } else if (value instanceof Date){
            cell.setCellValue((Date) value);

        } else if (value instanceof Boolean){
            cell.setCellValue((Boolean) value);

        } else if (value instanceof String){
            cell.setCellValue((String) value);

        } else if (value == null){
            //le type devient BLANK
            cell.setCellValue((String) null);
        }

        return cell;

    }

    /**
     * Ecrit une formule dans une cellule
     * @param sheet sheet
     * @param ri ligne
     * @param ci colonne
     * @param formula Formule
     */
    public static Cell writeWithFormula(Sheet sheet, Integer ri, Integer ci, String formula){
        Row row = sheet.getRow(ri);
        if (row == null){
            row = sheet.createRow(ri);
        }

        Cell cell = row.getCell(ci);
        if (cell == null) {
            cell = row.createCell(ci);
        }

        //cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(formula);

        return cell;
    }
}

