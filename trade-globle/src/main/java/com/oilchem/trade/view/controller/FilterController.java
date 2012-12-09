package com.oilchem.trade.view.controller;

import com.oilchem.trade.service.CommonService;
import com.oilchem.trade.service.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-12-6
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/manage")
public class FilterController extends CommonController {

    Logger logger = LoggerFactory.getLogger(FilterController.class);

    @Autowired
    FilterService filterService;
    @Autowired
    CommonService commonService;

    @RequestMapping("/list/{type}")
    public String list(Model model, @PathVariable String type) {

        findAllEntity(model.addAttribute("type", type), type);
        return "manage/trade/listfilter";
    }

    @RequestMapping("/list/allfilter")
    public String listAll(Model model) {
        String[] types = {"city", "country", "companyType", "customs", "tradeType", "transportation", "sumType"};
        for (String type : types) {
            findAllEntity(model, type);
        }
        return "manage/trade/listfilter";
    }

    /**
     * 添加记录
     * @param name
     * @param type
     * @param redirectAttrs
     * @return
     */
    @RequestMapping("/add/{type}/{name}")
    public String add(@PathVariable String name,
                      @PathVariable String type,
                      RedirectAttributes redirectAttrs){

        StringBuffer message = new StringBuffer();

        try{
            commonService.add(type,name);
            message.append("添加" + name + "成功");

        }   catch (Exception e){
            message.append("添加" + name + "失败");
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }

        redirectAttrs.addFlashAttribute("message", message.toString());
        return "redirect:/manage/list/" + type;
    }

    /**
     * 修改记录
     * @param name
     * @param type
     * @param id
     * @param redirectAttrs
     * @return
     */
    @RequestMapping("/update/{type}/{id}/{name}")
    public String update(@PathVariable String name,
                         @PathVariable String type,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttrs) {
        StringBuffer message = new StringBuffer();

        try {
            commonService.update(type,id, name);
            message.append("更新为" + name + "成功");

        } catch (Exception e) {
            message.append("更新为" + name + "失败");
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }

        redirectAttrs.addFlashAttribute("message", message.toString());
        return "redirect:/manage/list/" + type;
    }

    /**
     * 删除记录
     * @param type
     * @param id
     * @param redirectAttrs
     * @return
     */
    @RequestMapping("/del/{type}/{id}/{name}")
    public String del(@PathVariable String type,
                      @PathVariable Long id,
                      @PathVariable String name,
                      RedirectAttributes redirectAttrs) {

        StringBuffer message = new StringBuffer();

        try {
            commonService.delete(type, id);

            message.append("删除"+name+"成功");
        } catch (Exception e) {
            message.append("删除"+name+"失败");
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }

        redirectAttrs.addFlashAttribute("message", message.toString());
        return "redirect:/manage/list/" + type;
    }

}