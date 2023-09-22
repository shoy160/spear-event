package cn.spear.event.business.utils;

import cn.spear.event.business.domain.dto.EventCreateDTO;
import cn.spear.event.core.exception.BusinessException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sisa
 * @date 2022/12/30
 */
public class ExcelUtils {

    private final static Map<String, String> FIELD_MAPPING = new HashMap<>();
    private final static String DOT = ".";
    private final static String SUFFIX = "xlsx";

    static {
        FIELD_MAPPING.put("事件名", "name");
        FIELD_MAPPING.put("事件编码", "code");
        FIELD_MAPPING.put("事件源", "source");
        FIELD_MAPPING.put("说明", "desc");
        FIELD_MAPPING.put("模块", "tags");
    }

    public static void checkFile(String fileName) {
        if (CharSequenceUtil.isBlank(fileName)) {
            throw new BusinessException("文件名为空");
        }
        if (!fileName.contains(DOT)
                || !Objects.equals(SUFFIX, fileName.substring(fileName.lastIndexOf(DOT)+1))) {
            throw new BusinessException("仅支持 .xlsx 文件导入");
        }
    }

    public static List<EventCreateDTO> importEvent(InputStream in) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(in);
        XSSFSheet sheet = wb.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        List<EventCreateDTO> events = new ArrayList<>();
        // 获取数据对应关系
        Map<String, Integer> dataMapping = getDataMapping(sheet);
        // 获取数据
        for (int i = 1; i < lastRowNum; i++) {
            XSSFRow row = sheet.getRow(i);
            if (null != row) {
                XSSFCell nameCell = row.getCell(dataMapping.get("name"));
                XSSFCell codeCell = row.getCell(dataMapping.get("code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                XSSFCell descCell = row.getCell(dataMapping.get("desc"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                XSSFCell tagsCell = row.getCell(dataMapping.get("tags"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (StrUtil.isEmpty(codeCell.getStringCellValue())) {
                    continue;
                }
                EventCreateDTO source = new EventCreateDTO();
                source.setPartition(2);
                source.setRetention(60);
                source.setName(nameCell.getStringCellValue());
                String codeValue = codeCell.getStringCellValue();
                String code = codeValue.startsWith("authing") ? codeValue : "authing." + codeValue;
                source.setCode(code.trim());
                String tags = tagsCell.getStringCellValue();
                if (CharSequenceUtil.isNotBlank(tags)) {
                    String[] tag = tags.split(",");
                    source.setTags(Arrays.stream(tag).collect(Collectors.toList()));
                }
                source.setDesc(descCell.getStringCellValue());
                events.add(source);
            }
        }
        if (CollUtil.isEmpty(events)) {
            return new ArrayList<>();
        }
        return events.stream()
                .filter(i -> StrUtil.isNotBlank(i.getName()) && StrUtil.isNotBlank(i.getCode()))
                .collect(Collectors.toList());
    }

    private static Map<String, Integer> getDataMapping(XSSFSheet sheet) {
        Map<String, Integer> dataMapping = new HashMap<>(0);
        XSSFRow title = sheet.getRow(0);
        short lastCellNum = title.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            XSSFCell cell = title.getCell(i);
            String name = cell.getStringCellValue();
            String fieldName = FIELD_MAPPING.get(name);
            if (CharSequenceUtil.isNotBlank(fieldName)) {
                dataMapping.put(fieldName, i);
            }
        }
        return dataMapping;
    }
}
