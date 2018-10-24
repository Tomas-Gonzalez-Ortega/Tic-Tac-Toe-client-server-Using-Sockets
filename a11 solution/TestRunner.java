import java.util.*;
import java.util.stream.*;
import java.time.*;
import java.lang.reflect.*;

/** Runs a test harness.
	Runs all zero-argument methods who's names begin with "test".
	@author Jeremy H @ langara for CPSC 1181
	@version 2017-06-21 11:30am
*/
public class TestRunner implements Runnable {

	private final Class<?> clazz;
	private final List<AssertionError> failures;
	private final List<Throwable> errors;
	private int run = 0;
	private int passed = 0;

	public TestRunner(Class<?> clazz) {
		this.clazz = clazz;
		this.failures = new LinkedList<AssertionError>();
		this.errors = new LinkedList<Throwable>();
	}

	public TestRunner(String testClassName) throws ClassNotFoundException {
		this(Class.forName(testClassName));
	}

	public void run() {
		runTestsViaReflection();
	}

	private void runTestsViaReflection() {
		System.err.println("Running test harness: " + clazz.getName());

		final LocalDateTime start = LocalDateTime.now();

		// run each test method
		Stream.of(clazz.getDeclaredMethods())
			.filter(m -> m.getName().startsWith("test"))
			.filter(m -> m.getGenericParameterTypes().length == 0)
			.sequential()
			.forEach(this::runTest);

		final LocalDateTime end = LocalDateTime.now();
		final Duration dur = Duration.between(start, end);
		System.err.println("\nDuration: " + dur);

		if(failures.isEmpty() && errors.isEmpty()) {
			// all tests passed
			System.err.println(passed + " test(s) passed.");
		} else {
			// there were some failures or errors
			System.err.format("%d test(s) run: %d passed, %d failed, %d error(s).\n",
				run, passed, failures.size(), errors.size());
			System.err.println("\n***** FAILED *****\n");

			// print out their stack traces
			Stream.concat(failures.stream(), errors.stream())
				.sequential()
				.forEachOrdered(Throwable::printStackTrace);
		}
	}

	/** Invokes a method. */
	private void runTest(Method m) {
		try {
			// create an instance only if needed.
			Object instance = null;
			if((m.getModifiers() & Modifier.STATIC) != 0) {
				instance = null;	// static method, no instance needed
			} else {
				instance = clazz.newInstance();	// instance method
			}
			// make this method accessible incase it is not public
			m.setAccessible(true);
			// run the method
			run++;
			m.invoke(instance);
			passed++;
			// print an indicator
			System.err.print('.');
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			// error in invoking the method
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			// the method threw an exception
			Throwable cause = e.getCause();

			// remove the test runner's (and reflection's) methods from the stack trace
			int localStack = invokeThrowException().getStackTrace().length-2; //-2=invokeThrowException(),throwException()
			StackTraceElement[] oldStack = cause.getStackTrace();
			StackTraceElement[] newStack = new StackTraceElement[oldStack.length - localStack];
			System.arraycopy(oldStack, 0, newStack, 0, newStack.length);
			cause.setStackTrace(newStack);

			// record the failure or error
			if(cause instanceof AssertionError) {
				// an assertion failed
				failures.add((AssertionError) cause);
				System.err.print('X');
			} else {
				// the method threw some other exception
				errors.add(cause);
				System.err.print('E');
			}
		}
	}

	private static RuntimeException invokeThrowException() {
		try {
			TestRunner.class.getDeclaredMethod("throwException").invoke(null);
		} catch (InvocationTargetException e) {
			return (RuntimeException) e.getCause();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private static void throwException() {
		throw new RuntimeException();
	}

	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.err.println("Usage: java -ea TestRunner <testclassname>");
			System.exit(1);
		}
		new TestRunner(args[0]).run();
	}

	static {
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // Intentional side effect!!!
		if (!assertsEnabled) {
			throw new IllegalArgumentException("Asserts must be enabled!!! java -ea");
		}
	}
}
