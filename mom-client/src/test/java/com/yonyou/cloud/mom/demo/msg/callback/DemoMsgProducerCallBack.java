package com.yonyou.cloud.mom.demo.msg.callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yonyou.cloud.mom.client.producer.MqSender;
import com.yonyou.cloud.mom.core.dto.ProducerDto;
import com.yonyou.cloud.mom.core.store.StoreStatusEnum;
import com.yonyou.cloud.mom.core.store.callback.ProducerStoreDBCallback;
import com.yonyou.cloud.mom.core.store.callback.exception.StoreDBCallbackException;
import com.yonyou.cloud.mom.demo.dao.MsgDao;
import com.yonyou.cloud.mom.demo.msg.entity.MsgEntity;

@Service
@Transactional
public class DemoMsgProducerCallBack implements ProducerStoreDBCallback {
	Logger log = LoggerFactory.getLogger(DemoMsgProducerCallBack.class);

	@Autowired
	MsgDao msgDao;

	@Autowired
	private MqSender mqSender;

	@Override
	public void saveMsgData(String msgKey, String data, String exchange, String routerKey, String bizClassName)
			throws StoreDBCallbackException {
		System.out.println("进入存储消息的逻辑" + msgKey);
		MsgEntity msg = new MsgEntity();
		msg.setMsgKey(msgKey);
		msg.setExchange(exchange);
		msg.setRouterKey(routerKey);
		msg.setStatus(StoreStatusEnum.PRODUCER_INIT.getValue());
		msg.setRetryCount(0);
		msg.setCreateTime(new Date());
		msg.setBizClassName(bizClassName);
		msg.setMsgContent(data);
		msgDao.save(msg);
	}

	@Override
	public void update2success(String msgKey) throws StoreDBCallbackException {

		log.info("消息发送成功" + msgKey);
		MsgEntity msg = msgDao.findOne(msgKey);
		msg.setStatus(StoreStatusEnum.PRODUCER_SUCCESS.getValue());
		msgDao.save(msg);
	}

	@Override
	public void update2faild(String msgKey, String infoMsg, Long costTime, String exchange, String routerKey,
			String data, String bizClassName) throws StoreDBCallbackException {
		System.out.println("进入消息发送失败的逻辑" + msgKey);
		MsgEntity msg = new MsgEntity();
		msg.setMsgKey(msgKey);
		msg.setStatus(StoreStatusEnum.PRODUCER_FAILD.getValue());
		msg.setInfoMsg(infoMsg);
		msg.setUpdateTime(new Date());
		msg.setBizClassName(bizClassName);
		msg.setExchange(exchange);
		msg.setRouterKey(routerKey);
		msg.setMsgContent(data);
		msgDao.save(msg);
	}

	@Override
	public List<ProducerDto> selectResendList(Integer status) {

		log.info("扫描需要重新发送的消息" + status);

		List<ProducerDto> producerdtolist = new ArrayList<>();
		List<MsgEntity> list = msgDao.findbystatus(status);
		for (MsgEntity msg : list) {
			ProducerDto dto = new ProducerDto();
			dto.setExchange(msg.getExchange());
			dto.setMsgKey(msg.getMsgKey());
			dto.setRouterKey(msg.getRouterKey());
			dto.setMsgContent(msg.getMsgContent());
			dto.setBizClassName(msg.getBizClassName());
			dto.setMsgContent(msg.getMsgContent());
			producerdtolist.add(dto);
		}
		return producerdtolist;
	}
 
	@Override
	public ProducerDto getResendProducerDto(String msgkey) {
		MsgEntity producer=msgDao.findOne(msgkey);
		ProducerDto dto=new ProducerDto();
		BeanUtils.copyProperties(producer, dto);
		return 	dto;
	}

}
