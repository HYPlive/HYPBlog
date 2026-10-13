package top.hyp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hyp.entity.CommonWebsite;
import top.hyp.exception.PersistenceException;
import top.hyp.mapper.CommonWebsiteMapper;
import top.hyp.service.CommonWebsiteService;

import java.util.Date;
import java.util.List;

/**
 * @Description: 常用网站业务层实现
 * @Author: HYP
 * @Date: 2026-10-13
 */
@Service
public class CommonWebsiteServiceImpl implements CommonWebsiteService {
	@Autowired
	CommonWebsiteMapper commonWebsiteMapper;

	@Override
	public List<CommonWebsite> getWebsiteList() {
		return commonWebsiteMapper.getWebsiteList();
	}

	@Override
	public List<top.hyp.model.vo.CommonWebsite> getWebsiteVOList() {
		return commonWebsiteMapper.getWebsiteVOList();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveWebsite(CommonWebsite website) {
		website.setPublished(website.getPublished() == null || website.getPublished());
		website.setSort(website.getSort() == null ? 0 : website.getSort());
		website.setCreateTime(new Date());
		if (commonWebsiteMapper.saveWebsite(website) != 1) {
			throw new PersistenceException("添加失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateWebsite(CommonWebsite website) {
		if (commonWebsiteMapper.updateWebsite(website) != 1) {
			throw new PersistenceException("修改失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteWebsite(Long id) {
		if (commonWebsiteMapper.deleteWebsite(id) != 1) {
			throw new PersistenceException("删除失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updatePublishedById(Long id, Boolean published) {
		if (commonWebsiteMapper.updatePublishedById(id, published) != 1) {
			throw new PersistenceException("操作失败");
		}
	}
}
