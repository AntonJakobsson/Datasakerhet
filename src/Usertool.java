import sun.security.x509.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Usertool
{

	public static void main(String[] args)
	{
		int id = Integer.parseInt(args[0]);
		String name = args[1];
		int type = Integer.parseInt(args[2]);
		String division = args[3];
		String dn = "CN="+name+", OU="+division+", O=Sjughus, L=LHUND, S=SKÅNE, C=SK";
		
			KeyStore keyStore;
			try
			{
				keyStore = KeyStore.getInstance("JKS");
				keyStore.load(new FileInputStream("Keystore"), "password".toCharArray());
				KeyPair pair = generateKeyPair();
				X509Certificate cert = generateCertificate(id, dn, division, pair);
//				keyStore.setKeyEntry("key"+id+"", pair.getPrivate(), password, chain);
				keyStore.setCertificateEntry("CA", cert);
				keyStore.store(new FileOutputStream("Keystore"), "password".toCharArray());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		
		
	}

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException
	{
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("DSA");
		keyGenerator.initialize(1024);
		return keyGenerator.generateKeyPair();
	}

	public static X509Certificate generateCertificate(int id, String dn, String division, KeyPair pair)
			throws GeneralSecurityException, IOException
	{
		PrivateKey privkey = pair.getPrivate();
		  X509CertInfo info = new X509CertInfo();
		  Date from = new Date();
		  Date to = new Date(from.getTime() + 365 * 86400000l);
		  CertificateValidity interval = new CertificateValidity(from, to);
		  BigInteger sn = new BigInteger(64, new SecureRandom());
		  X500Name owner = new X500Name(dn);
		  
		  info.set(X509CertInfo.VALIDITY, interval);
		  info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		  info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		  info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		  info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
		  info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
		  AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
		  info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
		 
		  // Sign the cert to identify the algorithm that's used.
		  X509CertImpl cert = new X509CertImpl(info);
		  cert.sign(privkey, "SHA1withDSA");
		 
		  // Update the algorith, and resign.
		  algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
		  info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
		  cert = new X509CertImpl(info);
		  cert.sign(privkey, "SHA1withDSA");
		  return cert;
	}
}
