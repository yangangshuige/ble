package com.example.test.utils

import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.didi.bike.applicationholder.AppContextHolder
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {
    private val sMainHandler: Handler =Handler(Looper.getMainLooper())

    // 主线程Executor实例
    private val MAIN_EXECUTOR: Executor = ContextCompat.getMainExecutor(AppContextHolder.applicationContext())

    /**
     * 获取App主Handler
     * @return App主Handler
     */
    fun getMainHandler(): Handler {
        return sMainHandler
    }

    /**
     * 主线程Executor
     * @return 在主线程执行任务的Executor
     */
    fun getMainExecutor(): Executor {
        return MAIN_EXECUTOR
    }

    /**
     * 主线程Executor
     * @return IO型Executor
     */
    fun getIoExecutor(): Executor {
        return DEFAULT_IO_EXECUTOR
    }

    /**
     * 主线程Executor
     * @return 计算型Executor
     */
    fun getComputeExecutor(): Executor {
        return DEFAULT_COMPUTE_EXECUTOR
    }

    /** IO 执行器，主要执行IO相关操作，后续可对IO相关操作专门优化  */
    private val DEFAULT_IO_EXECUTOR: Executor = Executors.newFixedThreadPool(2)

    /** 计算执行器，计要执行Cpu计算相关命令。针对计算密集型挪作，手续可以专门优化  */
    private val DEFAULT_COMPUTE_EXECUTOR: Executor = Executors.newFixedThreadPool(2)

    /**
     * 在主执行器（主线程）执行命令
     * @param command　待执行的命令
     */
    fun executeOnMainExecutor(command: Runnable) {
        MAIN_EXECUTOR.execute(command)
    }

    /**
     * 在主执行器（主线程）延迟执行命令
     * @param command   待执行的命令
     * @param delayMillis   延迟的毫秒数
     */
    fun executeDelayedOnMainExecutor(command: Runnable, delayMillis: Long) {
        sMainHandler.postDelayed(command, delayMillis)
    }

    /**
     * 在计算执行器上执行命令
     * @param command 待执行的命令
     */
    fun executeOnComputeExecutor(command: Runnable) {
        DEFAULT_COMPUTE_EXECUTOR.execute(command)
    }

    /**
     * 在IO执行器上执行命令
     * @param command 待执行的命令
     */
    fun executeOnIoExecutor(command: Runnable) {
        DEFAULT_IO_EXECUTOR.execute(command)
    }

    companion object {
        val instance: AppExecutors by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppExecutors()
        }
    }
}