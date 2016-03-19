#include "DBPSServerError.h"
#include "..\Common\Log.h"

VOID ShowErrorWSAStartup(DWORD dwErr)
{
	switch (dwErr)
	{
	case WSASYSNOTREADY:
		ErrorLog("The underlying network subsystem is not ready for network communication.");
		break;
	case WSAVERNOTSUPPORTED:
		ErrorLog("The version of Windows Sockets support requested is not provided by this particular Windows Sockets implementation.");
		break;
	case WSAEINPROGRESS:
		ErrorLog("A blocking Windows Sockets 1.1 operation is in progress.");
		break;
	case WSAEPROCLIM:
		ErrorLog("A limit on the number of tasks supported by the Windows Sockets implementation has been reached.");
		break;
	case WSAEFAULT:
		ErrorLog("The lpWSAData parameter is not a valid pointer.");
		break;
	default:
		ErrorLog("Cannot find Error Type");
		break;
	}
}

VOID ShowErrorWSASocket(DWORD dwErr)
{
	switch (dwErr)
	{
	case WSANOTINITIALISED:
	case WSAENETDOWN:
	case WSAEAFNOSUPPORT:
	case WSAEFAULT:
	case WSAEINPROGRESS:
	case WSAEINVAL:
	case WSAEINVALIDPROVIDER:
	case WSAEINVALIDPROCTABLE:
	case WSAEMFILE:
	case WSAENOBUFS:
	case WSAEPROTONOSUPPORT:
	case WSAEPROTOTYPE:
	case WSAEPROVIDERFAILEDINIT:
	case WSAESOCKTNOSUPPORT:
		break;
	default:
		ErrorLog("Cannot find Error Type");
		break;
	}
}

VOID ShowErrorSetSockOpt(DWORD dwErr)
{
	switch (dwErr)
	{
	case WSANOTINITIALISED:
		ErrorLog("A successful WSAStartup call must occur before using this function.");
		break;
	case WSAENETDOWN:
		ErrorLog("The network subsystem has failed.");
		break;
	case WSAEFAULT:
		ErrorLog("The buffer pointed to by the optval parameter is not in a valid part of the process address space or the optlen parameter is too small.");
		break;
	case WSAEINPROGRESS:
		ErrorLog("A blocking Windows Sockets 1.1 call is in progress, or the service provider is still processing a callback function.");
		break;
	case WSAEINVAL:
		ErrorLog("The level parameter is not valid, or the information in the buffer pointed to by the optval parameter is not valid.");
		break;
	case WSAENETRESET:
		ErrorLog("The connection has timed out when SO_KEEPALIVE is set.");
		break;
	case WSAENOPROTOOPT:
		ErrorLog("The option is unknown or unsupported for the specified provider or socket (see SO_GROUP_PRIORITY limitations).");
		break;
	case WSAENOTCONN:
		ErrorLog("The connection has been reset when SO_KEEPALIVE is set.");
		break;
	case WSAENOTSOCK:
		ErrorLog("The descriptor is not a socket.");
		break;
	default:
		ErrorLog("Cannot find Error Type");
		break;
	}
}

VOID ShowErrorSend(DWORD dwErr)
{
	switch (dwErr)
	{
	case WSANOTINITIALISED:
		ErrorLog("A successful WSAStartup call must occur before using this function.");
		break;
	case WSAENETDOWN:
		ErrorLog("The network subsystem has failed.");
		break;
	case WSAEACCES:
		ErrorLog("The requested address is a broadcast address, but the appropriate flag was not set. Call setsockopt with the SO_BROADCAST socket option to enable use of the broadcast address.");
		break;
	case WSAEINTR:
		ErrorLog("A blocking Windows Sockets 1.1 call was canceled through WSACancelBlockingCall.");
		break;
	case WSAEINPROGRESS:
		ErrorLog("A blocking Windows Sockets 1.1 call is in progress, or the service provider is still processing a callback function.");
		break;
	case WSAEFAULT:
		ErrorLog("The buf parameter is not completely contained in a valid part of the user address space.");
		break;
	case WSAENETRESET:
		ErrorLog("The connection has been broken due to the keep-alive activity detecting a failure while the operation was in progress."); 
		break;
	case WSAENOBUFS:
		ErrorLog("No buffer space is available.");
		break;
	case WSAENOTCONN:
		ErrorLog("The socket is not connected.");
		break;
	case WSAENOTSOCK:
		ErrorLog("The descriptor is not a socket.");
		break;
	case WSAEOPNOTSUPP:
		ErrorLog("MSG_OOB was specified, but the socket is not stream-style such as type SOCK_STREAM, OOB data is not supported in the communication domain associated with this socket, or the socket is unidirectional and supports only receive operations.");
		break;
	case WSAESHUTDOWN:
		ErrorLog("The socket has been shut down; it is not possible to send on a socket after shutdown has been invoked with how set to SD_SEND or SD_BOTH.");
		break;
	case WSAEWOULDBLOCK:
		ErrorLog("The socket is marked as nonblocking and the requested operation would block.");
		break;
	case WSAEMSGSIZE:
		ErrorLog("The socket is message oriented, and the message is larger than the maximum supported by the underlying transport.");
		break;
	case WSAEHOSTUNREACH:
		ErrorLog("The remote host cannot be reached from this host at this time.");
		break;
	case WSAEINVAL:
		ErrorLog("The socket has not been bound with bind, or an unknown flag was specified, or MSG_OOB was specified for a socket with SO_OOBINLINE enabled.");
		break;
	case WSAECONNABORTED:
		ErrorLog("The virtual circuit was terminated due to a time-out or other failure. The application should close the socket as it is no longer usable.");
		break;
	case WSAECONNRESET:
		ErrorLog("The virtual circuit was reset by the remote side executing a hard or abortive close. For UDP sockets, the remote host was unable to deliver a previously sent UDP datagram and responded with a \"Port Unreachable\" ICMP packet. The application should close the socket as it is no longer usable.");
		break;
	case WSAETIMEDOUT:
		ErrorLog("The connection has been dropped, because of a network failure or because the system on the other end went down without notice.");
		break;
	default:
		ErrorLog("Cannot find Error Type");
		break;
	}
}