package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.MysqlDataSourceConfig;
import com.hitales.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@PropertySource("classpath:config/service.properties")
public abstract class BaseService extends GenericService {

    @Value("${page.size}")
    private int pageSize;
    @Value("${datasource.list}")
    private String dataSourceList;

    private Object basicInfo;
    private String xmlPath;

    /**
     * 用于createTime
     */
    protected Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    private ExecutorService threadPool;

    @Override
    public boolean processData() {
        initProcess();
        try {
            /*
             * 一个服务提供一个线程池,当前线程池关闭后无法再次使用
             */
            threadPool = Executors.newFixedThreadPool(8);
            String dataSourceList = getDataSourceList();
            String[] dataSourceArray = dataSourceList.split(",");
            //execute data process in every dataSource
            for (String dataSource : dataSourceArray) {
                this.process(dataSource);
            }
            //处理结束
            threadPool.shutdown();
            if (!threadPool.awaitTermination(24 * 3600, TimeUnit.SECONDS)) {
                // 超时的时候向线程池中所有的线程发出中断(interrupted)
                threadPool.shutdownNow();
            }
            //清空
            threadPool = null;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void initProcess(){

    }

    /**
     * 处理数据
     *
     * @param dataSource 数据源标识
     */
    protected void process(String dataSource) throws Exception {
        executeByMultiThread(getCount(dataSource), dataSource);
    }

    /**
     * 转换为需要入库的json类型
     *
     * @param entity
     * @return
     */
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    private void executeByMultiThread(Integer count, String dataSource) {
        if (count == 0) {
            log.error("executeByMultiThread(): count is 0");
            return;
        }
        int totalPage = 1;
        if (count > getPageSize()) {
            totalPage = count / getPageSize();
            int mod = count % getPageSize();
            if (mod > 0) {
                totalPage += 1;
            }
        }
        log.info("executeByMultiThread(): count:" + count + ",totalPage:" + totalPage);
        for (int i = 1; i < totalPage + 1; i++) {
            if (!threadPool.isShutdown()) {
                threadPool.execute(new storageRunnable(i, i + 1, dataSource));
            }
        }
    }

    /**
     * 通过数据源获取OdCategory
     *
     * @param dataSource
     * @return
     */
    protected String getOdCategory(String dataSource) {
        String odCategorie = EMPTY_FLAG;
        if (MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_XZDM;
        }
        if (MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_JKCT;
        }
        if (MysqlDataSourceConfig.MYSQL_YX_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YX;
        }
        if (MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YXZW;
        }
        if (MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_TNB;
        }
        if (MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_XZDM;
        }
        if (MysqlDataSourceConfig.MYSQL_GA_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_GA;
        }
        return odCategorie;
    }

    protected abstract void runStart(String dataSource, Integer startPage, Integer endPage);

    protected abstract Integer getCount(String dataSource);

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getDataSourceList() {
        return dataSourceList;
    }

    @Override
    public void setBasicInfo(Object pBasicInfo) {
        this.basicInfo = pBasicInfo;
    }

    public Object getBasicInfo() {
        return basicInfo;
    }

    public void setDataSourceList(String dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    class storageRunnable implements Runnable {
        private Integer startPage;
        private Integer endPage;
        private String dataSource;

        storageRunnable(Integer pStartPage, Integer pEndPage, String pDataSource) {
            this.startPage = pStartPage;
            this.endPage = pEndPage;
            this.dataSource = pDataSource;
        }

        @Override
        public void run() {
            runStart(this.dataSource, this.startPage, this.endPage);
        }
    }

    /*private static class ThreadPool {
        public static ExecutorService INSTANCE = Executors.newFixedThreadPool(8);

        private ThreadPool() {
        }

        public static ExecutorService getInstance() {
            return ThreadPool.INSTANCE;
        }
    }*/
}
