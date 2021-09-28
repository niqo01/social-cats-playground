# Setup Terraform service account

export GC_PROJECT_ID=testterraform2

gcloud config set project $GC_PROJECT_ID

gcloud iam service-accounts create terraform-account \
    --description="Service Account for terraform command" \
    --display-name="Terraform Service Account"
    
gcloud projects add-iam-policy-binding $GC_PROJECT_ID \
	--member="serviceAccount:terraform-account@${GC_PROJECT_ID}.iam.gserviceaccount.com" \
	--role="roles/editor"
	
gcloud projects add-iam-policy-binding $GC_PROJECT_ID \
	--member="serviceAccount:terraform-account@${GC_PROJECT_ID}.iam.gserviceaccount.com" \
	--role="roles/iam.serviceAccountAdmin"
	
gcloud iam service-accounts keys create terraform-key.json \
    --iam-account=terraform-account@${GC_PROJECT_ID}.iam.gserviceaccount.com
    
    
    
# Check service accounts roles

gcloud projects get-iam-policy $GC_PROJECT_ID  \
--flatten="bindings[].members" \
--format='table(bindings.role)' \
--filter="bindings.members:terraform-account@${GC_PROJECT_ID}.iam.gserviceaccount.com"




