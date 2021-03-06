package com.oilchem.trade.service.impl;

import com.oilchem.trade.dao.condition.*;
import com.oilchem.trade.domain.condition.*;
import com.oilchem.trade.util.EHCacheUtil;
import com.oilchem.trade.dao.*;
import com.oilchem.trade.dao.others.map.AbstractTradeDetailRowMapper;
import com.oilchem.trade.dao.others.map.MyRowMapper;
import com.oilchem.trade.domain.*;
import com.oilchem.trade.domain.abstrac.TradeDetail;
import com.oilchem.trade.domain.abstrac.TradeSum;
import com.oilchem.trade.domain.abstrac.IdEntity;
import com.oilchem.trade.service.CommonService;
import com.oilchem.trade.bean.DetailCriteria;
import com.oilchem.trade.bean.YearMonthDto;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static com.oilchem.trade.bean.DocBean.Config.*;
import static com.oilchem.trade.bean.DocBean.ExcelFiled.*;
import static com.oilchem.trade.bean.DocBean.AccessField.*;
import static com.oilchem.trade.bean.DocBean.Flag.unextract_flag;
import static com.oilchem.trade.bean.DocBean.Flag.unimport_flag;
import static com.oilchem.trade.bean.DocBean.TableType.detail;
import static com.oilchem.trade.bean.DocBean.TableType.sum;
import static com.oilchem.trade.service.impl.AppContextManager.getAppContext;
import static com.oilchem.trade.util.FileUtil.getFileSuffix;
import static com.oilchem.trade.util.FileUtil.upload;
import static com.oilchem.trade.util.QueryUtils.PropertyFilter;
import static com.oilchem.trade.util.QueryUtils.Type.GE;
import static com.oilchem.trade.util.QueryUtils.Type.LE;
import static com.oilchem.trade.util.ZipUtil.unRar;
import static com.oilchem.trade.util.ZipUtil.unZip;
import static org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-5
 * Time: 下午5:42
 * To change this template use File | Settings | File Templates.
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    CityDao cityDao;
    @Resource
    CompanyTypeDao companyTypeDao;
    @Resource
    CountryDao countryDao;
    @Resource
    CustomsDao customsDao;
    @Resource
    TradeTypeDao tradeTypeDao;
    @Resource
    TransportationDao transportationDao;
    @Resource
    SumTypeDao sumTypeDao;
    @Resource
    LogDao logDao;

    //日志记录器
    Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 上传文件
     *
     * @param file         MultipartFile的文件
     * @param realDir      目标目录的物理路径
     * @param yearMonthDto
     * @return 返回上传之后文件的url
     * @author wei.luo
     * @createTime 2012-11-7
     */
    public String uploadFile(MultipartFile file, String realDir, YearMonthDto yearMonthDto) {
        if (file == null || StringUtils.isBlank(realDir)) return null;

        String fileUrl = upload(file, realDir, root_url.value());
        return fileUrl;
    }

    /**
     * 解包
     *
     * @param logEntry
     * @param unPackageDir 解压目录
     * @return 解压后的文件路径
     */
    //@Before加锁
    //@After解锁
    public String unpackageFile(Map.Entry<Long, Log> logEntry, String unPackageDir) {

        if (logEntry == null || StringUtils.isBlank(unPackageDir))
            return null;

        String uploadPath = logEntry.getValue().getUploadPath();
        String type = getFileSuffix(uploadPath);

        //判断文件类型
        if (type.equals(".zip")) {
            return unZip(uploadPath, unPackageDir, null);
        } else if (type.equals(".rar")) {
            return unRar(uploadPath, unPackageDir);
        }else if(type.equals(".xls") || type.equals(".xlsx")){
              return uploadPath;
        }else return null;
    }

    /**
     * 导入查询条件表
     *
     * @param sql
     * @param accessPath
     * @return
     */
    @Transactional
    public void
    importCriteriaTab(String sql, String accessPath) {
        if (StringUtils.isBlank(sql) || StringUtils.isBlank(accessPath))
            return;

        ApplicationContext ctx = getAppContext();
        List<DetailCriteria> detailCriteriaList = new ArrayList<DetailCriteria>();

        try {
            //城市
            DetailCriteria cityCri = new DetailCriteria(
                    access_city.getValue(),
                    City.class,
                    CityDao.class,
                    CityDao.class.getDeclaredMethod("findByCity", String.class),
                    ctx.getBean(CityDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(cityCri);

            //国家
            DetailCriteria countryCri = new DetailCriteria(
                    access_country.getValue(),
                    Country.class,
                    CountryDao.class,
                    CountryDao.class.getDeclaredMethod("findByCountry", String.class),
                    ctx.getBean(CountryDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(countryCri);

            //企业性质
            DetailCriteria companyTypeCri = new DetailCriteria(
                    access_company_type.getValue(),
                    CompanyType.class,
                    CompanyTypeDao.class,
                    CompanyTypeDao.class.getDeclaredMethod("findByCompanyType", String.class),
                    ctx.getBean(CompanyTypeDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(companyTypeCri);

            //海关
            DetailCriteria customsCri = new DetailCriteria(
                    access_customs.getValue(),
                    Customs.class,
                    CustomsDao.class,
                    CustomsDao.class.getDeclaredMethod("findByCustoms", String.class),
                    ctx.getBean(CustomsDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(customsCri);

            //贸易类型
            DetailCriteria tradeTypeCri = new DetailCriteria(
                    access_trade_type.getValue(),
                    TradeType.class,
                    TradeTypeDao.class,
                    TradeTypeDao.class.getDeclaredMethod("findByTradeType", String.class),
                    ctx.getBean(TradeTypeDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(tradeTypeCri);

            //运输方式
            DetailCriteria transportationCri = new DetailCriteria(
                    access_transportation.getValue(),
                    Transportation.class,
                    TransportationDao.class,
                    TransportationDao.class.getDeclaredMethod("findByTransportation", String.class),
                    ctx.getBean(TransportationDao.class),
                    new HashSet<String>());
            detailCriteriaList.add(transportationCri);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        long before = System.currentTimeMillis();
        logger.info("执行前 before queryCriteriaRecord ******:" + before);

        //匹配
        queryCriteriaRecord(detailCriteriaList, sql, accessPath);

        long after = System.currentTimeMillis();
        logger.info("执行后 after queryCriteriaRecord ******:" + after);
        logger.info("用时 time:::" + (after - before) / 1000 + " s");

        //导入
        cityDao.save(nameList2IdEntityList(detailCriteriaList.get(0).getRetName(), City.class));
        countryDao.save(nameList2IdEntityList(detailCriteriaList.get(1).getRetName(), Country.class));
        companyTypeDao.save(nameList2IdEntityList(detailCriteriaList.get(2).getRetName(), CompanyType.class));
        customsDao.save(nameList2IdEntityList(detailCriteriaList.get(3).getRetName(), Customs.class));
        tradeTypeDao.save(nameList2IdEntityList(detailCriteriaList.get(4).getRetName(), TradeType.class));
        transportationDao.save(nameList2IdEntityList(detailCriteriaList.get(5).getRetName(), Transportation.class));

    }

    /**
     * name list 到 实例类 list的转换
     *
     * @param nameSet
     * @param idEntityClass
     * @param <E>
     * @return
     */
    private <E extends IdEntity> List<E>
    nameList2IdEntityList(Set<String> nameSet, Class<E> idEntityClass) {
        List<E> idEntityList = new ArrayList<E>();
        for (String name : nameSet) {
            try {
                idEntityList.add(idEntityClass.getConstructor(String.class).newInstance(name));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return idEntityList;
    }


    /**
     * 导入明细数据
     *
     * @param repository
     * @param tradeDetailMapper tradeDetailMapper
     * @param yearMonthDto
     * @param accessPath
     * @param sql               sql      @return  @author wei.luo
     * @param detailClz         明细抽象类
     */
    public <E extends TradeDetail, T extends AbstractTradeDetailRowMapper>
    Boolean importTradeDetail(
            final CrudRepository repository,
            T tradeDetailMapper, YearMonthDto yearMonthDto,
            String accessPath, String sql, Class detailClz) {

        Boolean isSuccess = true;

        if (tradeDetailMapper == null || yearMonthDto == null
                || StringUtils.isBlank(sql)) return false;

        long before = System.currentTimeMillis();
        logger.info("拆开并缓存 before queryCriteriaRecord ******:" + before);


        //拆开并缓存
        int idx = cacheListFormDB(
                tradeDetailMapper, yearMonthDto, accessPath, sql, detailClz);

        long after = System.currentTimeMillis();
        logger.info("拆开并缓存 after queryCriteriaRecord ******:" + after);
        logger.info("拆开并缓存用时 time:::" + (after - before) / 1000 + " s");

        ExecutorService pool = Executors.newFixedThreadPool(idx);
        //从缓存中取出来，使用线程池执行保存
        for (int i = 1; i <= idx; i++) {
            Object obj = EHCacheUtil.getValue("detail_cache", "detail_list_" + i);

            if (obj != null) {
                final List<E> detailList = (List<E>) obj;
                if (detailList.isEmpty()) break;

                Future<Iterable<E>> future = pool.submit(new Callable<Iterable<E>>() {
                    public Iterable<E> call() throws Exception {

                        Iterable<E> list = repository.save(detailList);
                        if (list != null) {
                            return list;
                        } else {
                            throw new RuntimeException("thread:[" + Thread.currentThread().getName()
                                    + "] save detail list fail......");
                        }
                    }
                });

                try {
                    isSuccess = isSuccess && future.get().iterator().hasNext();
                    if (future.isDone()) {
                        EHCacheUtil.removeElment("detail_cache", "detail_list_" + i);
                        detailList.clear();
                        Runtime.getRuntime().gc();
                    }
                } catch (Exception e) {
                    isSuccess = false;
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

            }
        }
        pool.shutdown();
        return isSuccess;
    }
//    volatile Boolean isDone = true;


    /**
     * 导入Excel
     *
     * @param repository
     * @param tradeSumDao
     * @param logEntry
     * @param tradeSumClass       tradeSum Class
     * @param tradeSumRowMapClass tradeSumRowMap Class
     * @param yearMonthDto        @return 成功或失败
     */
    public <E extends TradeSum, M extends MyRowMapper<E>>
    Boolean importExcel(CrudRepository repository,
                        BaseDao<E> tradeSumDao,
                        Map.Entry<Long, Log> logEntry,
                        Class<E> tradeSumClass,
                        Class<M> tradeSumRowMapClass,
                        YearMonthDto yearMonthDto) {
        if (tradeSumDao == null || tradeSumClass == null
                || tradeSumRowMapClass == null || yearMonthDto == null
                || logEntry == null)
            return null;

        Boolean isSuccess = true;

        //excel取数据
        List<E> tradeSumList = getListFromExcel(logEntry,
                tradeSumClass, tradeSumRowMapClass, yearMonthDto);
        isSuccess = isSuccess && (tradeSumList != null && !tradeSumList.isEmpty());

        //保存数据
        isSuccess = isSuccess && repository.save(tradeSumList) != null;

        return isSuccess;
    }

    /**
     * 获得未解压的文件列表
     *
     * @param tableType@return 返回记录的Id与包的全路径组成的Map
     */
    public Map<Long, Log> getUnExtractPackage(String tableType) {
        if (tableType == null) return null;

        Method findByMethod = null;
        try {

            findByMethod = LogDao.class.getDeclaredMethod(
                    "findByExtractFlagAndTableType", String.class, String.class);

        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return getLogMap(tableType, unextract_flag.getValue(),
                findByMethod);
    }

    /**
     * 获得未导入的文件列表
     *
     * @param tableType@return 返回记录的Id与文件的全路径组成的Map
     */
    public Map<Long, Log> getUnImportFile(String tableType) {
        if (StringUtils.isBlank(tableType)) return null;
        Method findByMethod = null;
        try {

            findByMethod = LogDao.class.getDeclaredMethod(
                    "findByImportFlagAndTableType", String.class, String.class);

        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return getLogMap(tableType, unimport_flag.getValue(),
                findByMethod);
    }

    /**
     * 获得logMap
     *
     * @param tableType
     * @param process_flag
     * @param findByMethod
     * @return
     * @throws Exception
     */
    private Map<Long, Log> getLogMap(String tableType, String process_flag,
                                     Method findByMethod) {

        if (StringUtils.isBlank(tableType) || StringUtils.isBlank(process_flag))
            return null;

        Map<Long, Log> packaeMap = new HashMap<Long, Log>();
        List<Log> logList = null;
        Object obj = null;

        //查找操作
        try {
            if (tableType.equals(detail.getValue())) {
                obj = findByMethod.invoke(logDao, process_flag, detail.getValue());
            } else if (tableType.equals(sum.getValue())) {
                obj = findByMethod.invoke(logDao, process_flag, sum.getValue());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (obj != null)
            logList = (List<Log>) obj;

        //把记录放到map中
        if (logList != null && !logList.isEmpty()) {
            for (Log log : logList) {
                packaeMap.put(log.getId(), log);
            }
        }

        return packaeMap;
    }

    /**
     * 获得数据模型的数据列表
     *
     * @param daoClass     daoClass
     * @param idEntityName
     * @return
     */
    public <T extends IdEntity> List<T> findAllIdEntityList(
            Class daoClass, String idEntityName) {
        if (daoClass == null) return null;

        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, idEntityName));

        List<T> idEntityList = null;
        try {
            Object obj = daoClass.getMethod("findAll", Sort.class)
                    .invoke(getCurrentWebApplicationContext().getBean(daoClass), sort);
            if (obj != null) {
                idEntityList = (List<T>) obj;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return idEntityList;
    }

    /**
     * 更新实体
     *
     * @param daoClass
     * @param entity
     * @param <T>
     */
    public <T extends IdEntity> void updateEntity(Class daoClass, T entity) {

        Object obj = null;
        try {
            obj = daoClass.getMethod("save", entity.getClass())
                    .invoke(getCurrentWebApplicationContext().getBean(daoClass), entity);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
//        entity.getClass().cast(obj);
    }


    private String domainPkgName = City.class.getPackage().getName();
    private String daoPkgName = CityDao.class.getPackage().getName();

    public <E> List<E> findAllEntityList(String type) {

        //约定：传入的type值必须与name字段的名称相同
        String entityName = type.substring(0, 1).toUpperCase() + type.substring(1);
        String daoClassName = daoPkgName + "." + entityName + "Dao";
        String entityClassName = domainPkgName + "." + entityName;

        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, type));
        List<E> idEntityList = null;
        try {
            Class daoClass = Class.forName(daoClassName);

            Object obj = daoClass.getMethod("findAll", Sort.class)
                    .invoke(getCurrentWebApplicationContext().getBean(daoClass), sort);
            if (obj != null) {
                idEntityList = (List<E>) obj;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return idEntityList;
    }

    /**
     * 添加一条记录
     *
     * @param type
     * @param name
     */
    public void add(String type, String name) {
        //约定：传入的type值必须与name字段的名称相同
        String entityName = type.substring(0, 1).toUpperCase() + type.substring(1);
        String daoClassName = daoPkgName + "." + entityName + "Dao";
        String entityClassName = domainPkgName + "." + entityName;
        try {
            Class daoClass = Class.forName(daoClassName);
            Class entityClass = Class.forName(entityClassName);

            Object daoObj = getCurrentWebApplicationContext().getBean(daoClass);
            Object entityObj = entityClass.getConstructor(String.class).newInstance(name);

            daoClass.getMethod("save", Object.class).invoke(daoObj, entityObj);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }


    /**
     * 更新指定id的记录
     *
     * @param type
     * @param name
     */
    public void update(
            String type, Long id, String name) {

        //约定：传入的type值必须与name字段的名称相同
        String entityName = type.substring(0, 1).toUpperCase() + type.substring(1);
        String daoClassName = daoPkgName + "." + entityName + "Dao";
        String entityClassName = domainPkgName + "." + entityName;
        try {
            Class daoClass = Class.forName(daoClassName);
            Class entityClass = Class.forName(entityClassName);

            Object daoObj = getCurrentWebApplicationContext().getBean(daoClass);
//            Object daoObj =  getAppContext().getBean(daoClass);

            Object entityObj = daoClass.getMethod("findOne", Serializable.class).invoke(daoObj, id);
            entityClass.getMethod("set" + entityName, String.class).invoke(entityObj, name);
            daoClass.getMethod("save", Object.class).invoke(daoObj, entityObj);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }


    /**
     * 删除指定id的记录
     *
     * @param type
     * @param id
     */
    public void delete(String type, Long id) {

        //约定：传入的type值必须与name字段的名称相同
        String entityName = type.substring(0, 1).toUpperCase() + type.substring(1);
        String daoClassName = daoPkgName + "." + entityName + "Dao";

        try {
            Class daoClass = Class.forName(daoClassName);
            Object daoObj = getCurrentWebApplicationContext().getBean(daoClass);
            daoClass.getMethod("delete", Serializable.class).invoke(daoObj, id);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 删除一条记录
     *
     * @param daoClass
     * @param id
     * @param <T>
     */
    public <T extends IdEntity> void delEntity(Class daoClass, Long id) {

        try {
            daoClass.getMethod("delete", Long.class)
                    .invoke(getCurrentWebApplicationContext().getBean(daoClass), id);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 从Access获得过滤后查询条件数据
     *
     * @param detailCriteriaList
     * @param sql
     * @param accessPath
     * @return
     */
    volatile ResultSet rs = null;

    private void
    queryCriteriaRecord(final List<DetailCriteria> detailCriteriaList,
                        String sql,
                        String accessPath) {

        if (accessPath == null || detailCriteriaList == null
                || StringUtils.isBlank(sql)) return;

        Connection conn = getDBConnect(accessPath);
        Statement statement = null;

        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);

            ExecutorService pool = Executors.newFixedThreadPool(Integer.parseInt(thread_poolsize.value()));
            for (int i = 1; rs.next(); i++) {

                pool.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        fillDetailCriteriaList(detailCriteriaList, rs);
                        return null;
                    }
                }).get();

            }
            pool.shutdown();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException();
        } finally {
            closeDBResource(conn, statement, rs);
        }
    }

    /**
     * 从Access表中获得明细数据list
     *
     * @param tradeDetailMapper tradeDetailMapper
     * @param yearMonthDto
     * @param accessPath
     * @param sql               sql
     * @param detailClz         detailClz   @return
     */
    public <E extends TradeDetail, T extends AbstractTradeDetailRowMapper> int
    cacheListFormDB(T tradeDetailMapper, YearMonthDto yearMonthDto,
                    String accessPath, String sql, Class detailClz) {
        //查出来然后导入
        Connection conn = getDBConnect(accessPath);
        Statement statement = null;
        ResultSet result = null;
        int j = 1;
        Integer listSize = Integer.parseInt(batch_updatesize.value());
        List<E> tradeDetailList = new ArrayList<E>(listSize);
        try {
            statement = conn.createStatement();
            result = statement.executeQuery(sql);
            EHCacheUtil.removeAllElment("detail_cache");
            for (int i = 1; result.next(); i++) {

                if(Boolean.valueOf(ignore_import_fail.value()).booleanValue()){
                    try {
                        fillTradeDetailList(result, tradeDetailMapper, yearMonthDto, detailClz, tradeDetailList);
                    } catch (Exception e) {
                        logger.error("此行记录导入发生错误:i=" + i + "  j=" + j);
                        int size = tradeDetailList.size() < 1 ? 1 : tradeDetailList.size();
                        String code = tradeDetailList.get(size - 1).getProductCode();
                        String country = tradeDetailList.get(size - 1).getCountry();
                        logger.error("产品代码与产销国家:code=" + code + " ,country=" + country);
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    fillTradeDetailList(result, tradeDetailMapper, yearMonthDto, detailClz, tradeDetailList);
                }



                if (i >= listSize) {
                    EHCacheUtil.setValue("detail_cache", "detail_list_" + j, tradeDetailList);
                    i = 1;
                    j++;
                    tradeDetailList = new ArrayList<E>(listSize);
                }


            }
            if (!tradeDetailList.isEmpty()) {
                EHCacheUtil.setValue("detail_cache", "detail_list_" + j, tradeDetailList);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeDBResource(conn, statement, result);
        }
        return j;
    }

    /**
     * 填充  tradeDetailList
     *
     * @param rs
     * @param tradeDetailMapper
     * @param yearMonthDto
     * @param detailClz
     * @param tradeDetailList
     * @param <E>
     * @param <T>
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private <E extends TradeDetail, T extends AbstractTradeDetailRowMapper>
    void fillTradeDetailList(ResultSet rs, T tradeDetailMapper,
                             YearMonthDto yearMonthDto, Class detailClz,
                             List<E> tradeDetailList)
            throws InstantiationException, IllegalAccessException, SQLException {

        E e = (E) detailClz.cast(detailClz.newInstance());
        tradeDetailMapper.setTraddDetail(e, rs,
                yearMonthDto.getYear(), yearMonthDto.getMonth());
        tradeDetailList.add(e);
    }

    /**
     * 填充 detailCriteriaList
     *
     * @param detailCriteriaList
     * @param rs
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void fillDetailCriteriaList(List<DetailCriteria> detailCriteriaList, ResultSet rs)
            throws SQLException, IllegalAccessException, InvocationTargetException {
        //取出每条记录中的条件字段，与条件表对应
        for (DetailCriteria detailCriteria : detailCriteriaList) {

            String name = rs.getString(detailCriteria.getFieldName());
            if (StringUtils.isBlank(name)) {
                continue;
            }
            Object findByMethodRet = detailCriteria.getFindByMethod()
                    .invoke(detailCriteria.getDao(), name.trim());

            Set<String> nameSet = detailCriteria.getRetName();
            //如果没有找到相同记录，则把name字段保存到IdEntity引用的对象中
            if (findByMethodRet == null) {
                nameSet.add(name.trim());
            }
        }
    }

    /**
     * 获得excel数据中的list
     *
     * @param logEntry
     * @param tradeSumClass
     * @param tradeSumRowMapClass
     * @param yearMonthDto
     * @return
     */
    private <E extends TradeSum, M extends MyRowMapper<E>> List<E>
    getListFromExcel(Map.Entry<Long, Log> logEntry,
                     Class<E> tradeSumClass,
                     Class<M> tradeSumRowMapClass,
                     YearMonthDto yearMonthDto) {

        //待导入的总表记录List
        List<E> tradeSumList = new ArrayList<E>();

        try {
            //从excel中取得eList
            Workbook workbook = Workbook.getWorkbook(
                    new File(logEntry.getValue().getExtractPath()));
            Sheet sheet = workbook.getSheet(0);
            int rows = sheet.getRows();
            int rowIdx = sheet.findCell(excel_product_name.getValue()).getRow() + 1;
            Integer year = yearMonthDto.getYear();
            Integer month = yearMonthDto.getMonth();
            String yearMonth = year + yearmonth_split.value() + (month < 10 ? "0" + month : month);

            //遍历excel
            for (; rowIdx < rows; rowIdx++) {
                E tradeSum = tradeSumClass.getConstructor(
                        Integer.class, Integer.class, String.class, String.class)
                        .newInstance(year, month, yearMonth,
                                yearMonthDto.getProductType());

                Constructor<M> constructor = tradeSumRowMapClass.getConstructor(
                        int.class, tradeSumClass, Sheet.class);
                M tradeSumMyRowMapper = constructor.newInstance(rowIdx, tradeSum, sheet);
                tradeSumList.add(tradeSumMyRowMapper.getMappingInstance());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return tradeSumList;
    }

    /**
     * 建立Access连接
     *
     * @param accessPath
     * @return
     */
    private Connection getDBConnect(String accessPath) {
        Connection conn;//连接参数
        Properties prop = new Properties();
        prop.put("charSet", "GBK");
        prop.put("user", "");
        prop.put("password", "");
        String url = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="
                + accessPath;

        //创建连接
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            conn = DriverManager.getConnection(url, prop);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return conn;
    }


    /**
     * 关闭statement与ResultSet
     *
     * @param statement
     * @param rs
     */
    private void closeDBResource(
            Connection conn, Statement statement, ResultSet rs) {
        try {
            if (rs == null) {
                rs.close();
            }
            if (statement == null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    /**
     * 设置year month属性到到查询的Properties当中去
     *
     * @param yearMonthDto
     */
    public List<PropertyFilter> getYearMonthQueryProps(YearMonthDto yearMonthDto) {

        List<PropertyFilter> filterList = new ArrayList<PropertyFilter>();

        if (yearMonthDto.getMonth() != null && yearMonthDto.getMonth() != 0) {
            filterList.add(new PropertyFilter("month", yearMonthDto.getMonth()));
        }

        if (yearMonthDto.getLowYear() != null) {
            Integer lowYear = yearMonthDto.getLowYear();
            Integer lowMonth = yearMonthDto.getLowMonth() == null ? 1 : yearMonthDto.getLowMonth();
            String yearMont = lowYear + yearmonth_split.value() + (lowMonth < 10 ? "0" + lowMonth : lowMonth);
            filterList.add(new PropertyFilter("yearMonth", yearMont, GE));
        }

        if (yearMonthDto.getHighYear() != null) {
            Integer highYear = yearMonthDto.getHighYear();
            Integer highMonth = yearMonthDto.getHighMonth() == null ? 1 : yearMonthDto.getHighMonth();
            String yearMonth = highYear + yearmonth_split.value() + (highMonth < 10 ? "0" + highMonth : highMonth);
            filterList.add(new PropertyFilter("yearMonth", yearMonth, LE));
        }

        return filterList;
    }
}
