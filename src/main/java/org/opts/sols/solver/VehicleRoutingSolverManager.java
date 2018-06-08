package org.opts.sols.solver;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.persistence.VehicleRoutingImporter;

@SuppressWarnings("serial")
@ApplicationScoped
public class VehicleRoutingSolverManager implements Serializable {

    private static final String SOLVER_CONFIG = "org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml";
    private String IMPORT_DATASET = "/org/opts/sols/data/problems_usa_zekleer1527861637145-road-km-d1-n75-k55.vrp";

    private SolverFactory<VehicleRoutingSolution> solverFactory;
    // After upgrading to JEE 7, replace ExecutorService by ManagedExecutorService:
    // @Resource(name = "DefaultManagedExecutorService")
    // private ManagedExecutorService executor;
    private ExecutorService executor;

    private Map<String, VehicleRoutingSolution> sessionSolutionMap;
    private Map<String, Solver<VehicleRoutingSolution>> sessionSolverMap;

    @PostConstruct
    public synchronized void init() {
        solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
        // Always terminate a solver after 2 minutes
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig().withMinutesSpentLimit(2L));
        executor = Executors.newFixedThreadPool(2); // Only 2 because the other examples have their own Executor
        // these probably don't need to be thread-safe because all access is synchronized
        sessionSolutionMap = new ConcurrentHashMap<>();
        sessionSolverMap = new ConcurrentHashMap<>();
    }

    @PreDestroy
    public synchronized void destroy() {
        for (Solver<VehicleRoutingSolution> solver : sessionSolverMap.values()) {
            solver.terminateEarly();
        }
        executor.shutdown();
    }

    public synchronized VehicleRoutingSolution retrieveOrCreateSolution(String sessionId, String DSURL) {
        VehicleRoutingSolution solution = sessionSolutionMap.get(sessionId);
        if (solution == null) {
            // URL unsolvedSolutionURL = getClass().getResource(DSURL);
            URL unsolvedSolutionURL = null;
			try {
				unsolvedSolutionURL = new URL("file:///"+DSURL);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (unsolvedSolutionURL == null) {
                throw new IllegalArgumentException("The IMPORT_DATASET (" + DSURL
                        + ") is not a valid classpath resource.");
            }
            solution = (VehicleRoutingSolution) new VehicleRoutingImporter()
                    .readSolution(unsolvedSolutionURL);
            sessionSolutionMap.put(sessionId, solution);
        }
        return solution;
    }

    public synchronized boolean solve(final String sessionId, String DSURL) {
        final Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();
        solver.addEventListener(new SolverEventListener<VehicleRoutingSolution>() {
			@Override
			public void bestSolutionChanged(BestSolutionChangedEvent<VehicleRoutingSolution> event) {
			    VehicleRoutingSolution bestSolution = event.getNewBestSolution();
			    synchronized (VehicleRoutingSolverManager.this) {
			        sessionSolutionMap.put(sessionId, bestSolution);
			    }
			}
		});
        if (sessionSolverMap.containsKey(sessionId)) {
            return false;
        }
        sessionSolverMap.put(sessionId, solver);
        final VehicleRoutingSolution solution = retrieveOrCreateSolution(sessionId,DSURL);
        executor.submit((Runnable) () -> {
            VehicleRoutingSolution bestSolution = solver.solve(solution);
            synchronized (VehicleRoutingSolverManager.this) {
                sessionSolutionMap.put(sessionId, bestSolution);
                sessionSolverMap.remove(sessionId);
            }
        });
        return true;
    }

    public synchronized boolean terminateEarly(String sessionId) {
        Solver<VehicleRoutingSolution> solver = sessionSolverMap.remove(sessionId);
        if (solver != null) {
            solver.terminateEarly();
            return true;
        } else {
            return false;
        }
    }

}
