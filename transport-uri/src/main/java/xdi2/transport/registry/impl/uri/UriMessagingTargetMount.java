package xdi2.transport.registry.impl.uri;

import xdi2.messaging.target.MessagingTarget;
import xdi2.transport.registry.MessagingTargetMount;

public class UriMessagingTargetMount implements MessagingTargetMount {

	private String messagingTargetPath;
	private MessagingTarget messagingTarget;

	public UriMessagingTargetMount(String messagingTargetPath, MessagingTarget messagingTarget) {

		this.messagingTargetPath = messagingTargetPath;
		this.messagingTarget = messagingTarget;
	}

	public UriMessagingTargetMount() {

	}

	public String getMessagingTargetPath() {

		return this.messagingTargetPath;
	}

	public void setMessagingTargetPath(String messagingTargetPath) {

		this.messagingTargetPath = messagingTargetPath;
	}

	@Override
	public MessagingTarget getMessagingTarget() {

		return this.messagingTarget;
	}

	public void setMessagingTarget(MessagingTarget messagingTarget) {

		this.messagingTarget = messagingTarget;
	}

	@Override
	public String toString() {

		return this.messagingTargetPath + " --> " + this.messagingTarget.getClass().getSimpleName();
	}
}
