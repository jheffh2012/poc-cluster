# Cluster com RabbitMQ + CastleMock + BackEnd/Lumen/PHP + FrontEnd/Angular/JS

## Dependências

### RabbitMQ Operator

Para a instalação do RabbitMQ Operator no cluster, forma seguidos os passos disponibilizados no seguinte tutorial: https://www.rabbitmq.com/kubernetes/operator/install-operator.html

### Camel K Operator
Para o funcionamento dos serviços de integração foram seguidos os passos disponíveis em: https://camel.apache.org/camel-k/latest/installation/installation.html

### NGINX Controller
O serviço que irá realizar o balanceamento e roteamento dos serviços disponíveis dentro do cluster é o NGINX. Para habilitar o serviço dentro do cluster, basta seguir o tutorial disponível em: https://kubernetes.github.io/ingress-nginx/deploy/

## Configurando o cluster

Iniciando com a configuração é necessário criar o serviço RabbitMQ dentro do cluster. Para isso basta executar o comando

```
kubectl apply -f ./code/definition.yaml
```

Após isso, precisamos buscar o usuário e senha para acessar o serviço rabbit, e também para usá-los para os serviços que irão se conectar com o rabbit, para isso os comandos abaixo devem ser executados:

```
kubectl get secret definition-default-user -o jsonpath="{.data.username}" | base64 --decode

kubectl get secret definition-default-user -o jsonpath="{.data.password}" | base64 --decode
```

Após isso, adicionaremos ao nosso cluster os serviços mysql e castlemock para banco de dados e mock de aplicações REST e SOAP respectivamente.
Para o MySQL:
```
kubectl apply -f ./code/mysql
```

Para o CastleMock:
```
kubectl apply -f ./code/castlemock
```

Após esses serviços instalados, iremos configurar a aplicação backend e frontend:

```
kubectl apply -f ./code/backend-ws
kubectl apply -f ./code/frontend
```

Após isso, vamos configurar o PHPMyAdmin:

```
kubectl apply -f ./code/phpmyadmin
```

Com isso, devemos acessar o PHPMyAdmin e criar um banco de dados chamado cluster,
após isso vamos acessar o container do backend para rodar as migrações do banco de dados.

```
kubectl exec -i -t POD-BACKEND-WS --container backend-teste -- /bin/bash
php artisan migrate
```


## Adicionando serviços de integração com legado e mensageria

Com o usuário e senha de acesso ao RabbitMQ, como também o vhost adicionado e/ou configurado, devem ser alterados os arquivos:

- code\integracao\boleto\servico.properties;
- code\integracao\rabbit\rabbitmq.properties.

Após isso, devem ser executados os seguintes arquivos:

- code\integracao\stur\run.sh;
- code\integracao\rabbit\run.sh;
- code\integracao\boleto\run.sh.

## Adicionando entradas para o cluster

Considerando que configuramos o NGINX como roteador das requisições realizadas ao servidor, deve ser executado o comando abaixo:

```
kubectl apply -f ./code/ingress.yaml
```