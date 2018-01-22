package com.platform.app.program.repository;

import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static com.platform.app.commontests.program.ProgramForTestsRepository.*;
import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.model.filter.PaginationData;
import com.platform.app.commontests.utils.TestBaseRepository;
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

        loadCustomersAndAdmins();
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addProgramAndFindIt() {
        final Program designPatterns = normalizeDependencies(program1(), em);
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return programRepository.add(designPatterns).getId();
        });
        assertThat(addedId, is(notNullValue()));

        final Program program = programRepository.findById(addedId);
        assertThat(program.getActiveApplication(), is(equalTo(designPatterns.getActiveApplication())));
        assertThat(program.getActiveCustomers(), is(equalTo(designPatterns.getActiveCustomers())));
        assertThat(program.getAdmin(), is(equalTo(designPatterns.getAdmin())));
        assertThat(program.getName(), is(equalTo(designPatterns.getName())));
        assertThat(program.getWaitingList().getList().size(), is(equalTo(0)));
    }

    @Test
    public void findProgramByIdNotFound() {
        final Program program = programRepository.findById(999L);
        assertThat(program, is(nullValue()));
    }

    @Test
    public void updateProgram() {
        final Program designPatterns = normalizeDependencies(program1(), em);
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return programRepository.add(designPatterns).getId();
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

    /*@Test
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
    }

    private void loadProgramsFindByFilter() {
        dbCommandExecutor.executeCommand(() -> {
            allPrograms().forEach((program) -> programRepository.add(normalizeDependencies(program, em)));
            return null;
        });
    }

    private void assertAuthors(final Book book, final Author... expectedAuthors) {
        final List<Author> authors = book.getAuthors();
        assertThat(authors.size(), is(equalTo(expectedAuthors.length)));

        for (int i = 0; i < expectedAuthors.length; i++) {
            final Author actualAuthor = authors.get(i);
            final Author expectedAuthor = expectedAuthors[i];
            assertThat(actualAuthor.getName(), is(equalTo(expectedAuthor.getName())));
        }
    }*/

    private void loadCustomersAndAdmins() {
        dbCommandExecutor.executeCommand(() -> {
            allCustomers().forEach(em::persist);
            allAdmins().forEach(em::persist);
            return null;
        });
    }

}
