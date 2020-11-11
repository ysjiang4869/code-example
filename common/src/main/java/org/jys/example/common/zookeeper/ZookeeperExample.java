package org.jys.example.common.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.TimeUnit;

/**
 * @author YueSong Jiang
 * @date 2019/3/13
 * Zookeeper curator use example
 */
public class ZookeeperExample {

    private CuratorFramework zkClient;


    public ZookeeperExample() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        zkClient.start();
    }

    public void watchChildrenForPath(String watchPath, boolean cacheData, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(zkClient, watchPath, cacheData);
        watcher.getListenable().addListener(listener);
        watcher.start();
    }

    public void createPath(String path, byte[] data) throws Exception {
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().
                    withMode(CreateMode.PERSISTENT).forPath(path, data);
        } else {
            throw new KeeperException.NodeExistsException();
        }
    }

    private void distributedLockExample() {
        String path = "/test/lock";
        InterProcessLock lock = new InterProcessMutex(zkClient, path);
        try {
            if (lock.acquire(1, TimeUnit.SECONDS)) {
                System.out.println("get lock");
            } else {
                System.out.println("get lock failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
                System.out.println("release lock");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void testWatch() {
        try {
            watchChildrenForPath("/test/watchChildrenForPath", false, (curatorFramework, pathChildrenCacheEvent) -> {
                ChildData childData = pathChildrenCacheEvent.getData();
                String path = childData.getPath();
                byte[] data = childData.getData();
                if (data == null) {
                    //when watchChildrenForPath some path, it will trigger the event when start
                    //but when start, the data will be null
                    data = zkClient.getData().forPath(path);
                }
                switch (pathChildrenCacheEvent.getType()) {
                    case CHILD_ADDED:
                        System.out.println("ADD");
                        break;
                    case CHILD_REMOVED:
                        System.out.println("remove");
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
