package com.platform.app.program.repository;

import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static com.platform.app.commontests.program.ApplicationForTestsRepository.allApps;
import static com.platform.app.commontests.program.ApplicationForTestsRepository.app2;
import static com.platform.app.commontests.program.ProgramForTestsRepository.*;

import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.model.filter.PaginationData;
import com.platform.app.commontests.utils.TestBaseRepository;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.program.model.Program;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProgramRepositoryUTest extends TestBaseRepository {

    private ProgramRepository programRepository;

    @Before
    public void initTestCase() {
        initializeTestDB();

        programRepository = new ProgramRepository();
        programRepository.em = em;

        persistDependencies();
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addProgramAndFindIt() {
        final Program toBeAdded = normalizeDependencies(program1(), em);
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return programRepository.add(toBeAdded).getId();
        });
        assertThat(addedId, is(notNullValue()));

        final Program program = programRepository.findById(addedId);
        assertThat(program.getActiveApplications(), is(equalTo(toBeAdded.getActiveApplications())));
        assertThat(program.getActiveCustomers(), is(equalTo(toBeAdded.getActiveCustomers())));
        assertThat(program.getAdmins(), is(equalTo(toBeAdded.getAdmins())));
        assertThat(program.getName(), is(equalTo(toBeAdded.getName())));
        assertThat(program.getWaitingList().getList().size(), is(equalTo(1)));
    }

    @Test
    public void findProgramByIdNotFound() {
        final Program program = programRepository.findById(999L);
        assertThat(program, is(nullValue()));
    }

    @Test
    public void updateProgram() {
        final Program toBeUpdated = normalizeDependencies(program1(), em);
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return programRepository.add(toBeUpdated).getId();
        });

        assertThat(addedId, is(notNullValue()));
        final Program program = programRepository.findById(addedId);
        assertThat(program.getName(), is(equalTo(program1().getName())));

        program.setName("Design Patterns");
        dbCommandExecutor.executeCommand(() -> {
            programRepository.update(program);
            return null;
        });

        final Program afterUpdate = programRepository.findById(addedId);
        assertThat(afterUpdate.getName(), is(equalTo("Design Patterns")));
    }

    @Test
    public void existsById() {
        final Program designPatterns = normalizeDependencies(program1(), em);
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return programRepository.add(designPatterns).getId();
        });

        assertThat(programRepository.existsById(addedId), is(equalTo(true)));
        assertThat(programRepository.existsById(999l), is(equalTo(false)));
    }

    @Test
    public void findProgramByName() {
        loadAllPrograms();
        Program temp = programRepository.findByName("program1");
        assertThat(temp.getName(), is(equalTo(program1().getName())));
    }

    @Test
    public void findProgramByWrongName() {
        loadAllPrograms();
        Program temp = programRepository.findByName("wrong program name");
        assertThat(temp, is(nullValue()));
    }

    @Test
    public void findProgramByAdmin() {
        loadAllPrograms();
        Program temp = programRepository.findByName("program1");
        List<Program> programs = programRepository.findByAdmin(temp.getAdmins().iterator().next());
        assertThat(programs.size(), is(equalTo(allPrograms().size())));
        for (Program p : programs) {
            assertThat(p.getName(), anyOf(is(program1().getName()), is(program2().getName())));
        }
    }

    @Test
    public void findProgramByApplication() {
        loadAllPrograms();
        Program temp = programRepository.findByName("program1");
        Program program = programRepository.findByApplication(temp.getActiveApplications().iterator().next());
        assertThat(program.getName(), is(equalTo("program1")));
    }

    @Test
    public void findProgramByWrongApplication() {
        loadAllPrograms();
        Program program = programRepository.findByApplication(app2());
        assertThat(program, is(nullValue()));
    }

    @Test
    public void findProgramByActiveUser() {
        loadAllPrograms();
        Program temp = programRepository.findByName("program1");
        List<Program> programs = programRepository.findByActiveUser(temp.getActiveCustomers().iterator().next());
        assertThat(programs.get(0).getName(), is(equalTo("program1")));
    }

    @Test
    public void findProgramByWrongUser() {
        loadAllPrograms();
        List<Program> programs = programRepository.findByActiveUser(mary());
        assertThat(programs.size(), is(equalTo(0)));
    }

    @Test
    public void deleteExistingProgram() {
        loadAllPrograms();
        assertThat(programRepository.findAll("name").size(), is(equalTo(2)));
        dbCommandExecutor.executeCommand(() -> {
            programRepository.delete(programRepository.findByName(program1().getName()));
            return null;
        });
        assertThat(programRepository.findAll("name").size(), is(equalTo(1)));
    }

    /*
    @Test
    public void findByFilterNoFilter() {
        loadProgramsFindByFilter();

        final PaginatedData<Program> result = programRepository.findByFilter(new ProgramFilter());
        assertThat(result.getNumberOfRows(), is(equalTo(5)));
        assertThat(result.getRows().size(), is(equalTo(5)));
        assertThat(result.getRow(0).getTitle(), is(equalTo(BookForTestsRepository.cleanCode().getTitle())));
        assertThat(result.getRow(1).getTitle(), is(equalTo(designPatterns().getTitle())));
        assertThat(result.getRow(2).getTitle(), is(equalTo(effectiveJava().getTitle())));
        assertThat(result.getRow(3).getTitle(), is(equalTo(peaa().getTitle())));
        assertThat(result.getRow(4).getTitle(), is(equalTo(refactoring().getTitle())));
    }

    @Test
    public void findByFilterWithPaging() {
        loadBooksForFindByFilter();

        final BookFilter bookFilter = new BookFilter();
        bookFilter.setPaginationData(new PaginationData(0, 3, "title", PaginationData.OrderMode.DESCENDING));
        PaginatedData<Book> result = bookRepository.findByFilter(bookFilter);

        assertThat(result.getNumberOfRows(), is(equalTo(5)));
        assertThat(result.getRows().size(), is(equalTo(3)));
        assertThat(result.getRow(0).getTitle(), is(equalTo(refactoring().getTitle())));
        assertThat(result.getRow(1).getTitle(), is(equalTo(peaa().getTitle())));
        assertThat(result.getRow(2).getTitle(), is(equalTo(effectiveJava().getTitle())));

        bookFilter.setPaginationData(new PaginationData(3, 3, "title", PaginationData.OrderMode.DESCENDING));
        result = bookRepository.findByFilter(bookFilter);

        assertThat(result.getNumberOfRows(), is(equalTo(5)));
        assertThat(result.getRows().size(), is(equalTo(2)));
        assertThat(result.getRow(0).getTitle(), is(equalTo(designPatterns().getTitle())));
        assertThat(result.getRow(1).getTitle(), is(equalTo(BookForTestsRepository.cleanCode().getTitle())));
    }

    @Test
    public void findByFilterFilteringByCategoryAndTitle() {
        loadBooksForFindByFilter();

        final Book book = new Book();
        book.setCategory(architecture());

        final BookFilter bookFilter = new BookFilter();
        bookFilter.setCategoryId(normalizeDependencies(book, em).getCategory().getId());
        bookFilter.setTitle("Software");
        bookFilter.setPaginationData(new PaginationData(0, 3, "title", PaginationData.OrderMode.ASCENDING));
        final PaginatedData<Book> result = bookRepository.findByFilter(bookFilter);

        assertThat(result.getNumberOfRows(), is(equalTo(1)));
        assertThat(result.getRows().size(), is(equalTo(1)));
        assertThat(result.getRow(0).getTitle(), is(equalTo(designPatterns().getTitle())));
    }*/

    /*private void assertAuthors(final Book book, final Author... expectedAuthors) {
        final List<Author> authors = book.getAuthors();
        assertThat(authors.size(), is(equalTo(expectedAuthors.length)));

        for (int i = 0; i < expectedAuthors.length; i++) {
            final Author actualAuthor = authors.get(i);
            final Author expectedAuthor = expectedAuthors[i];
            assertThat(actualAuthor.getName(), is(equalTo(expectedAuthor.getName())));
        }
    }*/

    private void loadAllPrograms() {
        dbCommandExecutor.executeCommand(() -> {
            allPrograms().forEach((program) -> programRepository.add(normalizeDependencies(program, em)));
            return null;
        });
    }

    private void persistDependencies() {
        dbCommandExecutor.executeCommand(() -> {
            allCustomers().forEach(em::persist);
            allAdmins().forEach(em::persist);
            allApps().forEach(em::persist);
            return null;
        });
    }

}
