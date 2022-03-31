package com.example.test.biz.viewmodel

import androidx.lifecycle.ViewModel

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   5/14/21
 * 全局唯一ViewModel基类
 * 使用方法：
 * <p>
 *     in activity or fragment scope:
 *     val boundDeviceViewModel: SomeProcessViewModel by processViewModels()
 *     ...
 */
abstract class ProcessViewModel() : ViewModel() {
    init {
        ProcessViewModelChecker.checkInstance(javaClass)
    }
}