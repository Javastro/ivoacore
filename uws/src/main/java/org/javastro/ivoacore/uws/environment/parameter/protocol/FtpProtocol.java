
package org.javastro.ivoacore.uws.environment.parameter.protocol;



/** Protocol Implementation for ftp:/
 * @todo replace with more robust implementation based on commons FtpClient.
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */
public class FtpProtocol extends HttpProtocol{

    /**
     * @see org.javastro.ivoacore.uws.parameter.protocol.Protocol#getProtocolName()
     */
    @Override
    public String getProtocolName() {
        return "ftp";
    }

}


