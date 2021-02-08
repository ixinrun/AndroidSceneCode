package com.example.db;

import java.util.List;

import com.example.entities.ThreadInfo;

/**
 * 数据访问接口
 * @author Administrator
 */
public interface ThreadDAO {
	/**
	 * 插入线程信息
	 * @param threadInfo
	 */
    public void insertThread(ThreadInfo threadInfo);
    /**
     * 删除线程
     * @param url
     * @param thread_id
     */
    public void deleteThread(String url,int thread_id);
    /**
     * 更新线程进度
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url,int thread_id,int finished);
    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);
    /**
     * 判断线程信息是否存在
     * @param url
     * @return
     */
    public boolean isExists(String url,int thread_id);
    
    
}
