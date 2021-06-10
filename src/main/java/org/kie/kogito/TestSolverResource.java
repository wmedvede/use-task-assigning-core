package org.kie.kogito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.taskassigning.core.model.ChainElement;
import org.kie.kogito.taskassigning.core.model.Group;
import org.kie.kogito.taskassigning.core.model.ModelConstants;
import org.kie.kogito.taskassigning.core.model.Task;
import org.kie.kogito.taskassigning.core.model.TaskAssigningSolution;
import org.kie.kogito.taskassigning.core.model.TaskAssignment;
import org.kie.kogito.taskassigning.core.model.User;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

@Path("/testSolver")
public class TestSolverResource {

    @Inject
    SolverFactory<TaskAssigningSolution> solverFactory;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String testSolver() {

        Solver<TaskAssigningSolution> solver = solverFactory.buildSolver();

        User user1 = new User("user1", true);
        user1.getGroups().add(new Group("managers"));

        User user2 = new User("user2", true);
        user2.getGroups().add(new Group("employees"));

        User user3 = new User("user3", true);

        TaskAssignment task1 = new TaskAssignment(Task.newBuilder()
                                                          .id("task1")
                                                          .name("Task1")
                                                          .potentialGroups(Collections.singleton("managers"))
                                                          .build());

        TaskAssignment task2 = new TaskAssignment(Task.newBuilder()
                                                          .id("task2")
                                                          .name("Task2")
                                                          .potentialGroups(Collections.singleton("employees"))
                                                          .build());


        user3.setNextElement(task1);

        task1.setUser(user3);
        task1.setStartTimeInMinutes(0);
        task1.setEndTimeInMinutes(1);
        task1.setPreviousElement(user3);
        task1.setNextElement(task2);

        task2.setUser(user3);
        task2.setStartTimeInMinutes(1);
        task2.setEndTimeInMinutes(2);
        task2.setPreviousElement(task1);

        TaskAssigningSolution solution = new TaskAssigningSolution("1", Arrays.asList(user1, user2, user3, ModelConstants.PLANNING_USER),
                                                                   Arrays.asList(task1, task2));

        TaskAssigningSolution result = solver.solve(solution);

        return "user1 tasks = " + getUserTasks(result, "user1") + ", user2 tasks = " + getUserTasks(result,"user2");
    }

    List<String> getUserTasks(TaskAssigningSolution solution, String userId) {
        List<String> result = new ArrayList<>();
        User user = solution.getUserList().stream()
                .filter(u -> u.getId().equals(userId))
                .findAny().orElseThrow(() -> new RuntimeException("user: " + userId + " was not found"));
        TaskAssignment next = user.getNextElement();
        while (next != null) {
            result.add(next.getId());
            next = next.getNextElement();
        }
        return result;
    }
}