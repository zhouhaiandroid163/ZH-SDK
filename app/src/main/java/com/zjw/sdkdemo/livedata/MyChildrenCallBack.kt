package com.zjw.sdkdemo.livedata

import com.zhapp.ble.bean.berry.children.ChallengeInfoBean
import com.zhapp.ble.bean.berry.children.ChallengeResultBean
import com.zhapp.ble.bean.berry.children.ChildrenInfoBean
import com.zhapp.ble.bean.berry.children.EarningsExchangeBean
import com.zhapp.ble.bean.berry.children.EarningsInfoBean
import com.zhapp.ble.bean.berry.children.FlashCardIdsBean
import com.zhapp.ble.bean.berry.children.FlashCardProgressBean
import com.zhapp.ble.bean.berry.children.MedalInfoBean
import com.zhapp.ble.bean.berry.children.ParentInfoBean
import com.zhapp.ble.bean.berry.children.SchedulerResultBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.ChildrenCallBack

object MyChildrenCallBack {

    val onChildrenInfo = UnFlawedLiveData<ChildrenInfoBean>()

    val onParentInfos = UnFlawedLiveData<ParentInfoBean>()

    val onFlashCardInfos = UnFlawedLiveData<FlashCardIdsBean>()

    val onFlashCardProgress = UnFlawedLiveData<FlashCardProgressBean>()

    val onChallengeInfos = UnFlawedLiveData<ChallengeInfoBean>()

    val onChallengeResult = UnFlawedLiveData<ChallengeResultBean>()

    val onEarningsInfo = UnFlawedLiveData<EarningsInfoBean>()

    val onChangePocketMoney = UnFlawedLiveData<EarningsExchangeBean>()

    val onSchedulerResult = UnFlawedLiveData<MutableList<SchedulerResultBean>>()

    val onMedalInfo = UnFlawedLiveData<MutableList<MedalInfoBean>>()

    fun initMyChildrenCallBack() {

        CallBackUtils.childrenCallBack = object : ChildrenCallBack {
            override fun onChildrenInfo(bean: ChildrenInfoBean) {
                onChildrenInfo.postValue(bean)
            }

            override fun onParentInfos(bean: ParentInfoBean) {
                onParentInfos.postValue(bean)
            }

            override fun onFlashCardInfos(bean: FlashCardIdsBean) {
                onFlashCardInfos.postValue(bean)
            }

            override fun onFlashCardProgress(bean: FlashCardProgressBean) {
                onFlashCardProgress.postValue(bean)
            }

            override fun onChallengeInfos(bean: ChallengeInfoBean) {
                onChallengeInfos.postValue(bean)
            }

            override fun onChallengeResult(bean: ChallengeResultBean) {
                onChallengeResult.postValue(bean)
            }

            override fun onEarningsInfo(bean: EarningsInfoBean) {
                onEarningsInfo.postValue(bean)
            }

            override fun onChangePocketMoney(bean: EarningsExchangeBean) {
                onChangePocketMoney.postValue(bean)
            }

            override fun onSchedulerResult(list: MutableList<SchedulerResultBean>) {
                onSchedulerResult.postValue(list)
            }

            override fun onMedalInfo(list: MutableList<MedalInfoBean>) {
                onMedalInfo.postValue(list)
            }
        }
    }
}