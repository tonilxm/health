package org.jhipster.health.web.rest;

import org.jhipster.health.Application;

import org.jhipster.health.domain.Points;
import org.jhipster.health.domain.User;
import org.jhipster.health.repository.PointsRepository;
import org.jhipster.health.repository.search.PointsSearchRepository;
import org.jhipster.health.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import org.jhipster.health.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Test class for the PointsResource REST controller.
 *
 * @see PointsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class PointsResourceIntTest {

    private static final LocalDate DEFAULT_TIMESTAMP = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TIMESTAMP = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_EXERCISE = 1;
    private static final Integer UPDATED_EXERCISE = 2;

    private static final Integer DEFAULT_MEALS = 1;
    private static final Integer UPDATED_MEALS = 2;

    private static final Integer DEFAULT_ALCOHOL = 1;
    private static final Integer UPDATED_ALCOHOL = 2;

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    @Autowired
    private PointsSearchRepository pointsSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc restPointsMockMvc;

    private Points points;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PointsResource pointsResource = new PointsResource(pointsRepository, pointsSearchRepository, userRepository);
        this.restPointsMockMvc = MockMvcBuilders.standaloneSetup(pointsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Points createEntity(EntityManager em) {
        Points points = new Points()
            .timestamp(DEFAULT_TIMESTAMP)
            .exercise(DEFAULT_EXERCISE)
            .meals(DEFAULT_MEALS)
            .alcohol(DEFAULT_ALCOHOL)
            .notes(DEFAULT_NOTES);
        return points;
    }

    @Before
    public void initTest() {
        pointsSearchRepository.deleteAll();
        points = createEntity(em);
    }

    @Test
    @Transactional
    public void createPoints() throws Exception {
        int databaseSizeBeforeCreate = pointsRepository.findAll().size();

        restPointsMockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Create security aware mockMVC
        restPointsMockMvc.perform(post("/api/points")
            .with(user("user"))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(points)))
            .andExpect(status().isCreated());

        // Validate the Points in the database
        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeCreate + 1);
        Points testPoints = pointsList.get(pointsList.size() - 1);
        assertThat(testPoints.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testPoints.getExercise()).isEqualTo(DEFAULT_EXERCISE);
        assertThat(testPoints.getMeals()).isEqualTo(DEFAULT_MEALS);
        assertThat(testPoints.getAlcohol()).isEqualTo(DEFAULT_ALCOHOL);
        assertThat(testPoints.getNotes()).isEqualTo(DEFAULT_NOTES);

        // Validate the Points in Elasticsearch
        Points pointsEs = pointsSearchRepository.findOne(testPoints.getId());
        assertThat(pointsEs).isEqualToComparingFieldByField(testPoints);
    }

    @Test
    @Transactional
    public void createPointsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pointsRepository.findAll().size();

        // Create the Points with an existing ID
        points.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPointsMockMvc.perform(post("/api/points")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(points)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTimestampIsRequired() throws Exception {
        int databaseSizeBeforeTest = pointsRepository.findAll().size();
        // set the field null
        points.setTimestamp(null);

        // Create the Points, which fails.

        restPointsMockMvc.perform(post("/api/points")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(points)))
            .andExpect(status().isBadRequest());

        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPoints() throws Exception {
        // Initialize the database
        pointsRepository.saveAndFlush(points);

        restPointsMockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Create security aware mockMVC
        restPointsMockMvc.perform(get("/api/points?sort=id,desc").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(points.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].exercise").value(hasItem(DEFAULT_EXERCISE)))
            .andExpect(jsonPath("$.[*].meals").value(hasItem(DEFAULT_MEALS)))
            .andExpect(jsonPath("$.[*].alcohol").value(hasItem(DEFAULT_ALCOHOL)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES.toString())));
    }

    @Test
    @Transactional
    public void getPoints() throws Exception {
        // Initialize the database
        pointsRepository.saveAndFlush(points);

        // Get the points
        restPointsMockMvc.perform(get("/api/points/{id}", points.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(points.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.exercise").value(DEFAULT_EXERCISE))
            .andExpect(jsonPath("$.meals").value(DEFAULT_MEALS))
            .andExpect(jsonPath("$.alcohol").value(DEFAULT_ALCOHOL))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPoints() throws Exception {
        // Get the points
        restPointsMockMvc.perform(get("/api/points/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePoints() throws Exception {
        // Initialize the database
        pointsRepository.saveAndFlush(points);
        pointsSearchRepository.save(points);
        int databaseSizeBeforeUpdate = pointsRepository.findAll().size();

        // Update the points
        Points updatedPoints = pointsRepository.findOne(points.getId());
        updatedPoints
            .timestamp(UPDATED_TIMESTAMP)
            .exercise(UPDATED_EXERCISE)
            .meals(UPDATED_MEALS)
            .alcohol(UPDATED_ALCOHOL)
            .notes(UPDATED_NOTES);

        restPointsMockMvc.perform(put("/api/points")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPoints)))
            .andExpect(status().isOk());

        // Validate the Points in the database
        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeUpdate);
        Points testPoints = pointsList.get(pointsList.size() - 1);
        assertThat(testPoints.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testPoints.getExercise()).isEqualTo(UPDATED_EXERCISE);
        assertThat(testPoints.getMeals()).isEqualTo(UPDATED_MEALS);
        assertThat(testPoints.getAlcohol()).isEqualTo(UPDATED_ALCOHOL);
        assertThat(testPoints.getNotes()).isEqualTo(UPDATED_NOTES);

        // Validate the Points in Elasticsearch
        Points pointsEs = pointsSearchRepository.findOne(testPoints.getId());
        assertThat(pointsEs).isEqualToComparingFieldByField(testPoints);
    }

    @Test
    @Transactional
    public void updateNonExistingPoints() throws Exception {
        int databaseSizeBeforeUpdate = pointsRepository.findAll().size();
        restPointsMockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Create the Points

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPointsMockMvc.perform(put("/api/points")
            .with(user("user"))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(points)))
            .andExpect(status().isCreated());

        // Validate the Points in the database
        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePoints() throws Exception {
        // Initialize the database
        pointsRepository.saveAndFlush(points);
        pointsSearchRepository.save(points);
        int databaseSizeBeforeDelete = pointsRepository.findAll().size();

        // Get the points
        restPointsMockMvc.perform(delete("/api/points/{id}", points.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean pointsExistsInEs = pointsSearchRepository.exists(points.getId());
        assertThat(pointsExistsInEs).isFalse();

        // Validate the database is empty
        List<Points> pointsList = pointsRepository.findAll();
        assertThat(pointsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPoints() throws Exception {
        // Initialize the database
        pointsRepository.saveAndFlush(points);
        pointsSearchRepository.save(points);

        // Search the points
        restPointsMockMvc.perform(get("/api/_search/points?query=id:" + points.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(points.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].exercise").value(hasItem(DEFAULT_EXERCISE)))
            .andExpect(jsonPath("$.[*].meals").value(hasItem(DEFAULT_MEALS)))
            .andExpect(jsonPath("$.[*].alcohol").value(hasItem(DEFAULT_ALCOHOL)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Points.class);
        Points points1 = new Points();
        points1.setId(1L);
        Points points2 = new Points();
        points2.setId(points1.getId());
        assertThat(points1).isEqualTo(points2);
        points2.setId(2L);
        assertThat(points1).isNotEqualTo(points2);
        points1.setId(null);
        assertThat(points1).isNotEqualTo(points2);
    }

    private void createPointsByWeek(LocalDate thisMonday, LocalDate lastMonday) {
        User user = userRepository.findOneByLogin("user").get();

        points = new Points(thisMonday.plusDays(2), 1, 1, 1, user);
        pointsRepository.saveAndFlush(points);

        points = new Points(thisMonday.plusDays(3), 1, 1, 0, user);
        pointsRepository.saveAndFlush(points);

        points = new Points(lastMonday.plusDays(3), 0, 0, 1, user);
        pointsRepository.saveAndFlush(points);

        points = new Points(lastMonday.plusDays(4), 1, 1, 0, user);
        pointsRepository.saveAndFlush(points);
    }

    public void getPointsThisWeek() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        createPointsByWeek(thisMonday, lastMonday);

        restPointsMockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Get all the points
        restPointsMockMvc.perform(get("/api/points")
            .with(user("user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)));

        // Get the points for this week only
        restPointsMockMvc.perform(get("/api/points-this-week")
            .with(user("user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.week").value(thisMonday.toString()))
            .andExpect(jsonPath("$.points").value(5));
    }

}
