package kdl.search;

import kdl.objects.KDLNode;
import org.mockito.ArgumentMatcher;

public class NodeIDMatcher implements ArgumentMatcher<KDLNode> {
	private final String id;

	private NodeIDMatcher(String id) {
		this.id = id;
	}

	@Override
	public boolean matches(KDLNode argument) {
		if (argument == null) {
			return false;
		}

		return id.equals(argument.getIdentifier());
	}

	public static NodeIDMatcher hasId(String id) {
		return new NodeIDMatcher(id);
	}
}
