package com.platform.app.geoIP.repository;

//import com.platform.app.category.model.Category;
//import com.platform.app.category.repository.CategoryRepository;

import com.platform.app.commontests.utils.TestBaseRepository;

//import static com.platform.app.commontests.category.CategoryForTestsRepository.*;

@Deprecated
public class GeoIPRepositoryUTest extends TestBaseRepository {
/*    private GeoIPRepository geoIPRepository;

    @Before
    public void initTestCase() {
        initializeTestDB();

        geoIPRepository = new GeoIPRepository();
        geoIPRepository.em = em;
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addGeoIPAndFindIt() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return geoIPRepository.add(test1()).getId();
        });

        assertThat(addedId, is(notNullValue()));

        final GeoIP geoIP = geoIPRepository.findById(addedId);
        assertThat(geoIP, is(notNullValue()));
        assertThat(geoIP.getIpAddress(), is(equalTo(test1().getIpAddress())));
    }

    @Test
    public void findGeoByIdNotFound() {
        final GeoIP geoIP = geoIPRepository.findById(999L);
        assertThat(geoIP, is(nullValue()));
    }

    @Test
    public void findGeoByIdWithNullId() {
        final GeoIP geoIP = geoIPRepository.findById(null);
        assertThat(geoIP, is(nullValue()));
    }

    @Test
    public void updateGeo() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return geoIPRepository.add(test1()).getId();
        });

        final GeoIP geoAfterAdd = geoIPRepository.findById(addedId);
        assertThat(geoAfterAdd.getCity(), is(equalTo(test1().getCity())));

        geoAfterAdd.setCity(test2().getCity());
        dbCommandExecutor.executeCommand(() -> {
            geoIPRepository.update(geoAfterAdd);
            return null;
        });

        final GeoIP geoAfterUpdate = geoIPRepository.findById(addedId);
        assertThat(geoAfterUpdate.getCity(), is(equalTo(test2().getCity())));
    }

    @Test
    public void findAllGeos() {
        dbCommandExecutor.executeCommand(() -> {
            allGeos().forEach(geoIPRepository::add);
            return null;
        });

        final List<GeoIP> geos = geoIPRepository.findAll("ipAddress");
        assertThat(geos.size(), is(equalTo(4)));
        assertThat(geos.get(0).getCity(), is(equalTo(test1().getCity())));
        assertThat(geos.get(1).getCity(), is(equalTo(test2().getCity())));
        assertThat(geos.get(2).getCity(), is(equalTo(test3().getCity())));
        assertThat(geos.get(3).getCity(), is(equalTo(test4().getCity())));
    }

    @Test
    public void alreadyExistsForAdd() {
        dbCommandExecutor.executeCommand(() -> {
            geoIPRepository.add(test1());
            return null;
        });

        assertThat(geoIPRepository.alreadyExists(test1()), is(equalTo(true)));
        assertThat(geoIPRepository.alreadyExists(test2()), is(equalTo(false)));
    }

    @Test
    public void alreadyExistsWithId() {
        final GeoIP geoIP = dbCommandExecutor.executeCommand(() -> {
            geoIPRepository.add(test2());
            return geoIPRepository.add(test1());
        });

        assertThat(geoIPRepository.alreadyExists(geoIP), is(equalTo(false)));

        geoIP.setIpAddress(test2().getIpAddress());
        assertThat(geoIPRepository.alreadyExists(geoIP), is(equalTo(true)));

        geoIP.setIpAddress(test3().getIpAddress());
        assertThat(geoIPRepository.alreadyExists(geoIP), is(equalTo(false)));
    }

    @Test
    public void existsById() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return geoIPRepository.add(test1()).getId();
        });

        assertThat(geoIPRepository.existsById(addedId), is(equalTo(true)));
        assertThat(geoIPRepository.existsById(999L), is(equalTo(false)));
    }

    @Test
    public void deleteExistingGeo() {
        final GeoIP geoIP = dbCommandExecutor.executeCommand(() -> {
            return geoIPRepository.add(test1());
        });
        dbCommandExecutor.executeCommand(() -> {
            geoIPRepository.delete(geoIPRepository.findById(geoIP.getId()));
            return null;
        });

        assertThat(geoIPRepository.findAll("ipAddress").size(),is(equalTo(0)));
    }
*/
}
