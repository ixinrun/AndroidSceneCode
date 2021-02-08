package com.example.db;

import java.util.List;

import com.example.entities.ThreadInfo;

/**
 * ���ݷ��ʽӿ�
 * @author Administrator
 */
public interface ThreadDAO {
	/**
	 * �����߳���Ϣ
	 * @param threadInfo
	 */
    public void insertThread(ThreadInfo threadInfo);
    /**
     * ɾ���߳�
     * @param url
     * @param thread_id
     */
    public void deleteThread(String url,int thread_id);
    /**
     * �����߳̽���
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url,int thread_id,int finished);
    /**
     * ��ѯ�ļ����߳���Ϣ
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);
    /**
     * �ж��߳���Ϣ�Ƿ����
     * @param url
     * @return
     */
    public boolean isExists(String url,int thread_id);
    
    
}
