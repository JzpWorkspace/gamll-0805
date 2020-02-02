package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class BaseAttrValueVo extends ProductAttrValueEntity {

    public void setValueSelected(List<String> valueSelected) {
        if (!CollectionUtils.isEmpty(valueSelected)) {
            //拼接字符串 以逗号进行分割
            this.setAttrValue(StringUtils.join(valueSelected, ","));
        } else {
            //如果是空那就返回为空
            this.setAttrValue(null);
        }
    }

}
