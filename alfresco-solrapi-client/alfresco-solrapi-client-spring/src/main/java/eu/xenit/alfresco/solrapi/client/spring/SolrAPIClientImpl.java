package eu.xenit.alfresco.solrapi.client.spring;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfresco.solrapi.client.spi.dto.Acl;
import eu.xenit.alfresco.solrapi.client.spi.dto.AclChangeSet;
import eu.xenit.alfresco.solrapi.client.spi.dto.AclChangeSets;
import eu.xenit.alfresco.solrapi.client.spi.dto.AclReaders;
import eu.xenit.alfresco.solrapi.client.spi.dto.AlfrescoModel;
import eu.xenit.alfresco.solrapi.client.spi.dto.AlfrescoModelDiff;
import eu.xenit.alfresco.solrapi.client.spi.dto.SolrNode;
import eu.xenit.alfresco.solrapi.client.spi.dto.SolrNodeList;
import eu.xenit.alfresco.solrapi.client.spi.dto.SolrNodeMetaData;
import eu.xenit.alfresco.solrapi.client.spi.query.NodeMetaDataQueryParameters;
import eu.xenit.alfresco.solrapi.client.spi.dto.SolrNodeMetadataList;
import eu.xenit.alfresco.solrapi.client.spi.query.NodesQueryParameters;
import eu.xenit.alfresco.solrapi.client.spi.SolrApiClient;
import eu.xenit.alfresco.solrapi.client.spi.dto.SolrTransactions;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class SolrAPIClientImpl implements SolrApiClient {

    private final String url;
    private final RestTemplate restTemplate;

    public SolrAPIClientImpl(SolrApiProperties solrApiProperties, SolrSslProperties solrSslProperties)
            throws GeneralSecurityException, IOException {
        this(solrApiProperties, new SolrRequestFactory(solrSslProperties));
    }

    public SolrAPIClientImpl(SolrApiProperties solrProperties, ClientHttpRequestFactory requestFactory) {
        this(solrProperties,
                new RestTemplateBuilder()
                        .requestFactory(() -> requestFactory)
                        .build());
    }

    public SolrAPIClientImpl(SolrApiProperties solrProperties, RestTemplate restTemplate) {
        this.url = solrProperties.getUrl();
        this.restTemplate = restTemplate;
    }

    private static HttpEntity<String> generateJsonHttpEntity(String query) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        return new HttpEntity<>(query, httpHeaders);
    }

    @Override
    public AclChangeSets getAclChangeSets(Long fromCommitTime, Long minAclChangeSetId, Long toCommitTime,
            Long maxAclChangeSetId, int maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Acl> getAcls(List<AclChangeSet> aclChangeSets, Long minAclId, int maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AclReaders> getAclReaders(List<Acl> acls) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SolrTransactions getTransactions(Long fromCommitTime, Long minTxnId, Long toCommitTime, Long maxTxnId,
            int maxResults) {
        log.debug("Get SolrTransactions [{},{}[", minTxnId, maxTxnId);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url + "transactions");
        conditionalQueryParam(uriBuilder, "fromCommitTime", fromCommitTime, Objects::nonNull);
        conditionalQueryParam(uriBuilder, "minTxnId", minTxnId, Objects::nonNull);
        conditionalQueryParam(uriBuilder, "toCommitTime", toCommitTime, Objects::nonNull);
        conditionalQueryParam(uriBuilder, "maxTxnId", maxTxnId, Objects::nonNull);
        conditionalQueryParam(uriBuilder, "maxResults", maxResults, val ->
                val != null && val != Integer.valueOf(0) && val != Integer.valueOf(Integer.MAX_VALUE));

        return restTemplate.getForObject(uriBuilder.toUriString(), SolrTransactions.class);
    }

    @Override
    public List<SolrNode> getNodes(NodesQueryParameters parameters) {
        log.debug("getNodes with " + parameters);

        SolrNodeList solrNodeList = restTemplate.postForObject(url + "nodes", parameters, SolrNodeList.class);
        Assert.notNull(solrNodeList, "Response for getNodes(" + parameters + ") should not be null");
        return solrNodeList.getNodes();
    }

    @Override
    public List<SolrNodeMetaData> getNodesMetaData(NodeMetaDataQueryParameters params) {
        log.debug("getNodesMetaData({})", params);
        SolrNodeMetadataList result = restTemplate.postForObject(
                url + "metadata", params, SolrNodeMetadataList.class);
        Assert.notNull(result, "Response for getNodes(" + params + ") should not be null");
        return result.getNodes();
    }

    @Override
    public AlfrescoModel getModel(String coreName, String modelName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AlfrescoModelDiff> getModelsDiff(String coreName, List<AlfrescoModel> currentModels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {

    }

    private UriComponentsBuilder conditionalQueryParam(UriComponentsBuilder builder, String key, Object value,
            Predicate<Object> condition) {
        Objects.requireNonNull(builder, "builder is required");
        Objects.requireNonNull(key, "key is required");
        if (value != null) {
            builder.queryParam(key, value);
        }
        return builder;
    }
}
