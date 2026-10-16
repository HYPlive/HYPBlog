package top.hyp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hyp.entity.CommonWebsite;

import java.util.List;

/**
 * @Description: 常用网站持久层接口
 * @Author: HYP
 * @Date: 2026-10-13
 */
@Mapper
@Repository
public interface CommonWebsiteMapper {
	List<CommonWebsite> getWebsiteList();

	List<top.hyp.model.vo.CommonWebsite> getWebsiteVOList();

	int saveWebsite(CommonWebsite website);

	int updateWebsite(CommonWebsite website);

	int deleteWebsite(Long id);

	int updatePublishedById(Long id, Boolean published);
}
