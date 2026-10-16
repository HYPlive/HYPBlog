package top.hyp.service;

import top.hyp.entity.CommonWebsite;

import java.util.List;

public interface CommonWebsiteService {
	List<CommonWebsite> getWebsiteList();

	List<top.hyp.model.vo.CommonWebsite> getWebsiteVOList();

	void saveWebsite(CommonWebsite website);

	void updateWebsite(CommonWebsite website);

	void deleteWebsite(Long id);

	void updatePublishedById(Long id, Boolean published);
}
