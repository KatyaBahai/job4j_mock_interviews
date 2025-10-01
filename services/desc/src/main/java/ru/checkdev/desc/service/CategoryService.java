package ru.checkdev.desc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.checkdev.desc.domain.Category;
import ru.checkdev.desc.domain.Topic;
import ru.checkdev.desc.dto.CategoryDTO;
import ru.checkdev.desc.dto.InterviewCountCategoryDto;
import ru.checkdev.desc.dto.InterviewCountDto;
import ru.checkdev.desc.repository.CategoryRepository;
import ru.checkdev.desc.utility.EurekaUriProvider;
import ru.checkdev.desc.utility.RestAuthCall;
import ru.checkdev.desc.utility.Utility;

import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TopicService topicService;
    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "mock";
    private static final String DIRECT = "/interviews/";


    public Optional<Category> findById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public void delete(int categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public void update(Category category) {
        categoryRepository.save(category);
    }

    public List<CategoryDTO> getAllCategoryDTO() {
        return categoryRepository.getAllCategoryDTO();
    }

    public List<InterviewCountCategoryDto> getMostPopular() {
        List<CategoryDTO> categoriesWithTopicCount = categoryRepository.findAllByOrderTotalDescLimit(
                PageRequest.of(0, Utility.LIMIT_MOST_POPULAR));

        Map<Integer, Integer> categoryInterviewCountPairs = getCategoryInterviewCountPairs();

        return categoriesWithTopicCount.stream()
                .map(cat -> InterviewCountCategoryDto.builder()
                        .id(cat.getId())
                        .interviewCount(categoryInterviewCountPairs.getOrDefault(cat.getId(), 0))
                        .name(cat.getName())
                        .position(cat.getPosition())
                        .topicsSize(cat.getTopicsSize())
                        .total(cat.getTotal())
                        .build())
                .collect(Collectors.toList());
    }

    public void updateStatistic(int id) {
        categoryRepository.updateStatistic(id);
    }

    private Map<Integer, Integer> getCategoryInterviewCountPairs() {
        try {
            String interviewCountString = new RestAuthCall(String
                    .format("%s%sinterviewsCountByTopic", uriProvider.getUri(SERVICE_ID), DIRECT))
                    .get();
            var mapper = new ObjectMapper();
            List<InterviewCountDto> interviewCounts = mapper.readValue(interviewCountString, new TypeReference<>() {
            });
            List<Topic> topics = topicService.getAll();

            Map<Integer, Category> topicToCategory = topics.stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getCategory));

            Map<Integer, Integer> categoryInterviewCountPairs = new HashMap<>();
            for (InterviewCountDto countDto : interviewCounts) {
                Category category = topicToCategory.get(countDto.getTopicId());
                if (category != null) {

                    categoryInterviewCountPairs.merge(category.getId(),
                            countDto.getInterviewCount(), Integer::sum);
                }
            }
            return categoryInterviewCountPairs;
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to parse interview count json response", e);
        }
    }
}
